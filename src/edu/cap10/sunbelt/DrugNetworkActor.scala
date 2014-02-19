package edu.cap10.sunbelt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import akka.actor.ActorContext
import akka.actor.Stash

case class Ack(msg:Any)
  
object Resources extends Enumeration { val Precursor, Drugs = Value }
import Resources.{Value => Resource, _}

case class Deliver(what:Resource, amount:Double, paid:Double) {
  lazy val book = Book(what, amount, paid)
}

case class Exchange(pay:Double, want:Resource) {
  def deliver(charge:Double = pay, amount:Double) = Deliver(want, amount, charge)
}

case class Deposit(what:Resource, amount:Double, value:Double)
case class Withdraw(what:Resource, amount:Double, value:Double) {
  lazy val deliver = Deliver(what,amount,value)
}
case class Book(what:Resource, amount:Double, price:Double) {
  lazy val deliver = Deliver(what, Math.abs(amount), price)
}


import scala.collection.mutable.{Map => MMap}

case class Entry(bought:Double = 0, sold:Double = 0, paid:Double = 0, got:Double = 0) {
  def +(dep:Deposit) = copy(bought = dep.amount, paid = dep.value)
  def +(wdw:Withdraw) = copy(sold = wdw.amount, got = wdw.value)
  def +(book:Book) = 
    if (book.amount < 0) 
      copy(sold = sold - book.amount, got = got + book.price)
    else
      copy(bought = bought + book.amount, paid = paid + book.price)

  override def toString() = bought + " " + sold + " " + paid + " " + got

}

object GangMember {

  type Ledger = MMap[Resource,Entry]
  def initLedger : Ledger = MMap(Resources.values.toSeq.map({ _ -> Entry() }):_*)
}

import GangMember._

case class init(l:Ledger)

sealed trait GangMember {
  def receive()(implicit context : ActorContext, ledger : Ledger) : Receive
}

import scala.util.{Try, Failure, Success}

case class ExpectDeliveries(from:Set[ActorRef] = Set.empty)

  case class World (
    precursorPerPaid:Double = 0,
    drugsWantedPerTime:Double,
    retailers : Set[ActorRef]
  ) extends GangMember {
    def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context.sender
      implicit def self : ActorRef = context.self
      
      {
	    case Tick(time) =>
	      val pay = ledger(Drugs).paid / ledger(Drugs).bought * drugsWantedPerTime
	      // use total history as expected price per quantity
	      val e = Exchange(pay/retailers.size, Drugs)
	      self ! ExpectDeliveries(retailers)
	      retailers foreach { _ ! e }
	    case Exchange( paid, Precursor) =>
	      self ! Withdraw(Precursor, precursorPerPaid*paid, paid)->sender
      }
	}
  } 

  case class Retailer (
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context.sender
      implicit def self : ActorRef = context.self
      
      {
        case Tick(time) =>   
          val estimatedSale = (ledger(Drugs).sold / (time+1))
          val estimatedLeft = ledger(Drugs).bought - ledger(Drugs).sold - estimatedSale
          // after this periods anticipated sale
          val estimatePurchase = estimatedSale - estimatedLeft
          // how much will needed for next period
          if (estimatePurchase > 0) {
            self ! ExpectDeliveries(Set(wholesaler))
            val pay = estimatePurchase * ledger(Drugs).paid / ledger(Drugs).bought
            wholesaler ! Exchange(pay, Drugs)
          } else {
            self ! ExpectDeliveries()
          }
        case Exchange( paid, Drugs ) =>
          self ! Net(paid*margin)
          val left = paid*(1-margin)
          val paidRate = ledger(Drugs).bought / ledger(Drugs).paid
          val amt = Math.min(ledger(Drugs).bought - ledger(Drugs).sold, paidRate*left)
          self ! Withdraw(Drugs, amt, amt/paidRate)->sender
      }
    }
  }
  
  final case class Wholesaler (
    middleman:ActorRef, 
    retailers:Set[ActorRef],
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context.sender
      implicit def self : ActorRef = context.self
      
      {
        case Tick(time) =>
          val estimatedSale = (ledger(Drugs).sold / (time+1))
          val estimatedLeft = ledger(Drugs).bought - ledger(Drugs).sold - estimatedSale
          // after this periods anticipated sale
          val estimatePurchase = estimatedSale - estimatedLeft
          // how much will needed for next period
          if (estimatePurchase > 0) {
            self ! ExpectDeliveries(Set(middleman))
            val pay = estimatePurchase * ledger(Drugs).paid / ledger(Drugs).bought
            middleman ! Exchange(pay, Drugs)
          } else {
            self ! ExpectDeliveries()
          }
        case Exchange( paid, Drugs ) =>
          self ! Net(paid*margin)
          val left = paid*(1-margin)
          val paidRate = ledger(Drugs).bought / ledger(Drugs).paid
          val amt = Math.min(ledger(Drugs).bought - ledger(Drugs).sold, paidRate*left)
          self ! Withdraw(Drugs, amt, amt/paidRate)->sender
      }
    }
  }
  
  final case class Middleman (
    wholesaler:ActorRef, 
    cook:ActorRef,
    suppliers:Set[ActorRef],
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context._
      {
        case Tick(time) =>
          self ! ExpectDeliveries(suppliers+cook)
//          val estimatedSale = (ledger(Drugs).sold / (time+1))
//          val estimatedLeft = ledger(Drugs).bought - ledger(Drugs).sold - estimatedSale
//          // after this periods anticipated sale
//          val estimatePurchase = estimatedSale - estimatedLeft
//          // how much will needed for next period
//          if (estimatePurchase > 0) {
//            self ! ExpectDeliveries(Set(middleman))
//            val pay = estimatePurchase * ledger(Drugs).paid / ledger(Drugs).bought
//            middleman ! Exchange(pay, Drugs)
//          } else {
//            self ! ExpectDeliveries()
//          }
        case Exchange( paid, Drugs ) =>
          self ! Net(paid*margin)
          val left = paid*(1-margin)
          val paidRate = ledger(Drugs).bought / ledger(Drugs).paid
          val amt = Math.min(ledger(Drugs).bought - ledger(Drugs).sold, paidRate*left)
          self ! Withdraw(Drugs, amt, amt/paidRate)->sender
          //val Entry(available, cost, other, yetanother) = ledger(Precursor)
      }
    }
  }
  
  final case class Supplier (
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context._

      {
        case Exchange( paid, Precursor ) =>
        case Deliver( Precursor, amt, cashBack ) =>
      }
	}
  }
  
  final case class Cook (
    middleman:ActorRef,
    margin:Double
  ) extends GangMember {
    // precursor sold column == consumed for drugs
    // drugs bought column == drugs produced from precursor
    // every tick, convert available precursor (up to peak) into drugs
	def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context._
      {
	    case Exchange( paid, Drugs ) =>
	    case Deliver( Precursor, amt, cashBack) =>
      }
	}
  }

  case class Net(amount:Double)
  case class Report(time:Long)
  
class GangActor extends Actor {
    
  var profit : Double = 0L;
  var Time : Long = 0L;
  
  def advance = {
    println(self.path.name + " " + ledger(Drugs) + " " + ledger(Precursor))
    context.parent ! Ack(Tick(Time))
    Time += 1
  }
  
  def net(amt:Double) = {
    profit += amt
  }
  
  import GangMember.Ledger
  implicit val ledger : Ledger = initLedger
  

  
//  import Entities._
  var awaiting = Set.empty[ActorRef]
  def receive = {
    case g : GangMember =>
      context become (g.receive() orElse receive)
    case ExpectDeliveries(from) if from.isEmpty => advance
    case ExpectDeliveries(from) if awaiting.isEmpty =>
      awaiting = from
    case d @ Deliver(what, amt, paid) if awaiting contains sender =>
      awaiting -= sender
      ledger(what) = ledger(what)+d.book
      if (awaiting.isEmpty) advance
    case d @ Deposit(what,_,_) =>
      ledger(what) = ledger(what)+d
    case w @ Withdraw(what,_,_) =>
      ledger(what) = ledger(what)+w
    case (w @ Withdraw(what,_,_), deliverTo:ActorRef) =>
      ledger(what) = ledger(what)+w
      deliverTo ! w.deliver
    case Net(amt) => net(amt)
    case Report(time) => println(time + " "+self.path.name + " net " + profit)
    
  }
  
}

case class Tick(time:Long)

class Runner extends Actor {
  //implicit val timeout = akka.util.Timeout(1000)
  val world :: middleman :: wholesaler :: cook :: Nil = List("world","middleman","wholesaler","cook").map( name => context.actorOf(Props[GangActor], name))
  val retailers = (1 to 1).map( i => context.actorOf(Props[GangActor],"retailer"+i) ).toSet
  val suppliers = (1 to 1).map( i => context.actorOf(Props[GangActor],"supplier"+i) ).toSet
  
  val drugsWantedPerTime = 18750 // assume one average medium lab, producing 625kg per day serve this slice of the world
  val precursorPerPaid = 1000/10000
  val middlemanMargin, wholesalerMargin, cookMargin, retailMargin, supplierMargin = 0.10
  
  // initialize the world
  world ! World(precursorPerPaid, drugsWantedPerTime, retailers)
  world ! Deposit(Drugs, 1, 10*50)

  retailers foreach { r => 
    r ! Retailer(wholesaler,world,retailMargin)
    r ! Deposit(Drugs, 2*18750, 2*18750*200)
    r ! Withdraw(Drugs, 18750, 18750*75*10)
  }
  
  wholesaler ! Wholesaler(middleman, retailers, wholesalerMargin)
  wholesaler ! Deposit(Drugs, 2*18750, 2*18750*200/(1+wholesalerMargin))
  wholesaler ! Withdraw(Drugs, 18750, 18750*200)
  
  middleman ! Middleman(wholesaler, cook, suppliers, middlemanMargin)
  middleman ! Deposit(Drugs, 2*18750, 2*18750*200/(1+wholesalerMargin)/(1+middlemanMargin))
  middleman ! Withdraw(Drugs, 18750, 18750*200/(1+wholesalerMargin))
  
  cook ! Cook(middleman, cookMargin)
  

  suppliers foreach { _ ! Supplier(middleman,world,supplierMargin) }
  
  //val all = (world +: middleman +: wholesaler +: cook +: (retailers ++ suppliers)).toSet
  val all : Set[ActorRef] = retailers + world + wholesaler
  
  def alerting(Time:Long = 0)(implicit group:Set[ActorRef]) : Receive = {
    case t @ Tick(10) => context.system.shutdown
    case t @ Tick(Time) =>
      context become awaiting(group, Time)
      group.filter(_ != world) foreach { _ ! Report(Time) } 
      group foreach { _ ! t }
    case m => println("strange in runner: "+ m)
  }
  
  override def receive = alerting(0)(all)
  
  def awaiting(who:Set[ActorRef], Time:Long)(implicit group:Set[ActorRef]) : Receive = {
    { case Ack(Tick(Time)) if (who contains sender) =>
	      who - sender match {
	        case s if s.isEmpty =>
	          context become alerting(Time+1)
	          self ! Tick(Time+1)
	        case s => context become awaiting(s,Time)
	      }
      case m => println("strange in runner awaiting: "+ m)
    }
  }
}
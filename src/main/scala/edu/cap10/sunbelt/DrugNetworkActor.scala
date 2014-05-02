package edu.cap10.sunbelt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import akka.actor.ActorContext
import akka.actor.Stash

import java.io.PrintStream

case class Ack(msg:Any)
  
object Resources extends Enumeration { val Precursor, Drugs = Value }
import Resources.{Value => Resource, _}

//case class Deliver(what:Resource, amount:Double, paid:Double) {
//  lazy val book = Deposit(what, amount, paid)
//}

case class Exchange(pay:Double, want:Resource, max:Double)

case class Deposit(what:Resource, amount:Double, value:Double)
case class Withdraw(what:Resource, amount:Double, value:Double) {
  lazy val deliver = Deposit(what,amount,value)
}

import scala.collection.mutable.{Map => MMap}

case class Entry(bought:Double = 0, sold:Double = 0, paid:Double = 0, got:Double = 0) {
  def +(dep:Deposit) = copy(bought = bought+dep.amount, paid = paid+dep.value)
  def +(wdw:Withdraw) = copy(sold = sold+wdw.amount, got = got+wdw.value)
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
  def split(res:Resource, ledger:Ledger, paid:Double, margin:Double, max:Double) = {
    val paidRate = ledger(res).bought / ledger(res).paid
    val amt = Math.min(Math.min(ledger(res).bought - ledger(res).sold, paidRate*paid/(1+margin)),max)
    
    val net = amt*margin/paidRate
    (Net(net), Withdraw(res, amt, amt*(1+margin)/paidRate))
  }
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
	      val avail = ledger(Drugs).bought - ledger(Drugs).sold
	      val consume = Math.min(avail, drugsWantedPerTime)
	      val left = avail - consume
	      println("world consumes "+consume+" ratio "+(consume/drugsWantedPerTime) + " stockpile "+left)
	      val pay = (ledger(Drugs).paid / ledger(Drugs).bought) * drugsWantedPerTime * (drugsWantedPerTime / drugsWantedPerTime)
	      // use total history as expected price per quantity
	      val e = Exchange(pay/retailers.size, Drugs, drugsWantedPerTime/retailers.size)
	      self ! ExpectDeliveries(retailers)
	      self ! Withdraw(Drugs, consume, 0)
	      retailers foreach { _ ! e }
	    case Exchange( paid, Precursor, max) =>
	      val amount = Math.min(paid*precursorPerPaid, max)
	      val pay = amount / precursorPerPaid
	      self ! Withdraw(Precursor, amount, pay)->sender
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
          val estimatePurchase = estimatedSale - estimatedLeft*(if (estimatedLeft < 0) 2 else 0.5)
          
          // how much will needed for next period
          if (estimatePurchase > 0) {
            self ! ExpectDeliveries(Set(wholesaler))
            val pay = estimatePurchase * ledger(Drugs).paid / ledger(Drugs).bought
            wholesaler ! Exchange(pay, Drugs, estimatePurchase)
          } else {
            self ! ExpectDeliveries()
          }
        case Exchange( paid, Drugs, max ) =>
          val (net, wdw) = split(Drugs,ledger, paid, margin, max)
          println("retail nets "+net.amount+" delivers "+wdw.amount)
          self ! net
          self ! wdw->sender
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
            middleman ! Exchange(pay, Drugs, estimatePurchase)
          } else {
            self ! ExpectDeliveries()
          }
        case Exchange( paid, Drugs, max ) =>
          val (net, wdw) = split(Drugs,ledger,paid,margin, max)
          self ! net
          self ! wdw->sender
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
      import context.sender
      implicit def self : ActorRef = context.self
      
      {
        case Tick(time) =>
//          self ! ExpectDeliveries(suppliers+cook)
          val cookOpt : Option[Exchange] = {
	          val estimatedSale = (ledger(Drugs).sold / (time+1))
	          val estimatedLeft = ledger(Drugs).bought - ledger(Drugs).sold - estimatedSale
	          // after this periods anticipated sale
	          val estimatePurchase = estimatedSale - estimatedLeft
	          // how much will needed for next period
	          if (estimatePurchase > 0) {
	            val pay = estimatePurchase * ledger(Drugs).paid / ledger(Drugs).bought
	            Some(Exchange(pay, Drugs, estimatePurchase))
	          } else {
	        	None
	          }
          }
          val suppliersOpt : Option[Exchange] = {
	          val estimatedSale = (ledger(Precursor).sold / (time+1))
	          val estimatedLeft = ledger(Precursor).bought - ledger(Precursor).sold - estimatedSale
	          // after this periods anticipated sale
	          val estimatePurchase = estimatedSale - estimatedLeft
	          // how much will needed for next period
	          if (estimatePurchase > 0) {
	            val pay = estimatePurchase * ledger(Precursor).paid / ledger(Precursor).bought
	            Some(Exchange(pay/suppliers.size, Precursor, estimatePurchase))
	          } else {
	        	None
	          }
          }
          (cookOpt, suppliersOpt) match {
            case (Some(eCook), Some(eSup)) =>
              self ! ExpectDeliveries(suppliers+cook)
              cook ! eCook
              suppliers foreach { _ ! eSup }
            case (None, Some(eSup)) =>
              self ! ExpectDeliveries(suppliers)
              suppliers foreach { _ ! eSup }
            case (Some(eCook), None) =>
              self ! ExpectDeliveries(Set(cook))
              cook ! eCook
            case _ => self ! ExpectDeliveries()
          } 
        case Exchange( paid, res , max) =>
          val (net, wdw) = split(res,ledger,paid,margin, max)
          self ! net
          self ! wdw->sender
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
      import context.sender
      implicit def self : ActorRef = context.self

      {
        case Tick(time) =>
          val estimatedSale = (ledger(Precursor).sold / (time+1))
          val estimatedLeft = ledger(Precursor).bought - ledger(Precursor).sold - estimatedSale
          // after this periods anticipated sale
          val estimatePurchase = estimatedSale - estimatedLeft
          // how much will needed for next period
          if (estimatePurchase > 0) {
            self ! ExpectDeliveries(Set(world))
            val pay = estimatePurchase * ledger(Precursor).paid / ledger(Precursor).bought
            world ! Exchange(pay, Precursor, estimatePurchase)
          } else {
            self ! ExpectDeliveries()
          }
        case Exchange( paid, Precursor, max ) =>
          val (net, wdw) = split(Precursor,ledger,paid,margin, max)
          self ! net
          self ! wdw->sender
      }
	}
  }
  
  final case class Cook (
    middleman:ActorRef,
    peakConversion:Double,
    drugsPerPrecursor:Double,
    margin:Double
  ) extends GangMember {
    
	def receive()(implicit context : ActorContext, ledger : Ledger) : Receive = {
      import context.sender
      implicit def self : ActorRef = context.self
      
      {
        case Tick(time) =>
          val avail = ledger(Precursor).bought - ledger(Precursor).sold
          val convoAmt = Math.min(avail, peakConversion)
          val atHistoricRate = convoAmt*ledger(Precursor).bought/ledger(Precursor).paid
          self ! Withdraw(Precursor, convoAmt, atHistoricRate)
          self ! Deposit(Drugs, convoAmt*drugsPerPrecursor, atHistoricRate)
          val left = avail - convoAmt
          val want = convoAmt - left
          if (want > 0) {
            self ! ExpectDeliveries(Set(middleman))
            middleman ! Exchange(want*ledger(Precursor).paid/ledger(Precursor).bought, Precursor, want)
          } else {
            self ! ExpectDeliveries()
          }
	    case Exchange( paid, Drugs, max ) =>
	      val (net, wdw) = split(Drugs,ledger,paid,margin, max)
          self ! net
          self ! wdw->sender
      }
	}
  }

  case class Net(amount:Double)
  case class Report(time:Long, tar:PrintStream)
  
class GangActor extends Actor {
    
  var profit : Double = 0L;
  var Time : Long = 0L;
  
  def advance = {
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
    case d @ Deposit(what, amt, paid) if awaiting contains sender =>
      awaiting -= sender
      ledger(what) = ledger(what)+d
      if (awaiting.isEmpty) advance
    case d @ Deposit(what,_,_) =>
      ledger(what) = ledger(what)+d
    case w @ Withdraw(what,_,_) =>
      ledger(what) = ledger(what)+w
    case (w @ Withdraw(what,_,_), deliverTo:ActorRef) =>
      ledger(what) = ledger(what)+w
      deliverTo ! w.deliver
    case Net(amt) => net(amt)
    case Report(time,pw) => pw.println(time + " "+self.path.name + " "+ profit+" " + ledger(Drugs) + " " + ledger(Precursor))
    
  }
  
}

case class Tick(time:Long)

class Runner extends Actor {
  val debugging = false
  val tarout = if (debugging) System.out else new PrintStream("output.txt")
  val MAXTIME : Long = if (debugging) 50 else 100
  //implicit val timeout = akka.util.Timeout(1000)
  val world :: middleman :: wholesaler :: cook :: Nil = List("world","middleman","wholesaler","cook").map( name => context.actorOf(Props[GangActor], name))
  val retailers = (1 to 1).map( i => context.actorOf(Props[GangActor],"retailer"+i) ).toSet
  val suppliers = (1 to 1).map( i => context.actorOf(Props[GangActor],"supplier"+i) ).toSet
  
  val cookOutputRate = 625*30 // assume one average medium lab, producing 625kg per day serve
  val drugsWantedPerTime = cookOutputRate  // this gangs slice of the world wants everything the lab produces
  val drugsPerPrecursor = 0.9
  val cookConversionRate = cookOutputRate / drugsPerPrecursor
  val precursorPerPaid = 0.5
  val middlemanMargin, wholesalerMargin, cookMargin, supplierMargin = 0.05
  val retailMargin = 1
  
  
  // initialize the world
  world ! Deposit(Drugs, 2*drugsWantedPerTime, 2*drugsWantedPerTime*500)
  world ! Withdraw(Drugs, drugsWantedPerTime, 0)
  world ! World(precursorPerPaid, drugsWantedPerTime, retailers)
  

  retailers foreach { r => 
    r ! Retailer(wholesaler,world,retailMargin)
    r ! Deposit(Drugs, 2*drugsWantedPerTime, 2*drugsWantedPerTime*200)
    r ! Withdraw(Drugs, drugsWantedPerTime, drugsWantedPerTime*50*10)
  }
  
  suppliers foreach { s => 
    s ! Supplier(middleman,world,supplierMargin)
    s ! Deposit(Precursor, 2*drugsWantedPerTime/drugsPerPrecursor, 2*drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid)
    s ! Withdraw(Precursor, drugsWantedPerTime/drugsPerPrecursor, drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid/(1+supplierMargin))
  }
  
  wholesaler ! Wholesaler(middleman, retailers, wholesalerMargin)
  wholesaler ! Deposit(Drugs, 2*drugsWantedPerTime, 2*drugsWantedPerTime*200/(1+wholesalerMargin))
  wholesaler ! Withdraw(Drugs, drugsWantedPerTime, drugsWantedPerTime*200)
  
  middleman ! Middleman(wholesaler, cook, suppliers, middlemanMargin)
  middleman ! Deposit(Drugs, 2*drugsWantedPerTime, 2*drugsWantedPerTime*200/(1+wholesalerMargin)/(1+middlemanMargin))
  middleman ! Withdraw(Drugs, drugsWantedPerTime, drugsWantedPerTime*200/(1+wholesalerMargin))
  middleman ! Deposit(Precursor, 2*drugsWantedPerTime/drugsPerPrecursor, 2*drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid/(1+supplierMargin)/(1+middlemanMargin))
  middleman ! Withdraw(Precursor, drugsWantedPerTime/drugsPerPrecursor, drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid/(1+supplierMargin))
  
  cook ! Cook(middleman, cookConversionRate, drugsPerPrecursor, cookMargin)
  cook ! Deposit(Precursor, 2*drugsWantedPerTime/drugsPerPrecursor, 2*drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid/(1+supplierMargin)/(1+middlemanMargin))
  cook ! Withdraw(Precursor, drugsWantedPerTime/drugsPerPrecursor, drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid/(1+supplierMargin)/(1+middlemanMargin))
  cook ! Deposit(Drugs, 2*drugsWantedPerTime, 2*drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid)
  cook ! Withdraw(Drugs, drugsWantedPerTime, drugsWantedPerTime/drugsPerPrecursor/precursorPerPaid)
  
  //val all = (world +: middleman +: wholesaler +: cook +: (retailers ++ suppliers)).toSet
  val all : Set[ActorRef] = suppliers ++ retailers + world + wholesaler + cook + middleman 
   
  def alerting(Time:Long = 0)(implicit group:Set[ActorRef]) : Receive = {
    case t @ Tick(MAXTIME) => 
      tarout.flush()
      tarout.close()
      context.system.shutdown
    case t @ Tick(Time) =>
      context become awaiting(group, Time)
      group foreach { _ ! Report(Time,tarout) } 
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
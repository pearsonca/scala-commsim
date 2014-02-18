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

case class Deposit(reserves:Double, cut:Double)
case class Book(what:Resource, amount:Double, price:Double) {
  lazy val deliver = Deliver(what, Math.abs(amount), price)
}


import scala.collection.mutable.{Map => MMap}

object GangMember {
  case class Entry(bought:Double = 0, sold:Double = 0, paid:Double = 0, got:Double = 0) {
    def +(book:Book) = if (book.amount < 0) 
        copy(sold = sold - book.amount, got = got + book.price)
      else
        copy(bought = bought + book.amount, paid = paid + book.price)
  }
  type Ledger = MMap[Resource,Entry]
  def initLedger : Ledger = MMap(Resources.values.toSeq.map({ _ -> Entry() }):_*)
}

import GangMember._

case class init(l:Ledger)

sealed trait GangMember {
  def receive()(implicit context : ActorContext) : Receive
}

import scala.util.{Try, Failure, Success}

case class ExpectDeliveries(from:Set[ActorRef] = Set.empty)

  case class World (
    precursorPerPaid:Double = 0,
    drugsWantedPerTime:Double,
    retailers : Set[ActorRef]
  ) extends GangMember {
    def receive()(implicit context : ActorContext) : Receive = {
      implicit def self : ActorRef = context.self
      
      {
	    case Tick(time) =>
	      println(self+" is world at "+time)
	      val averagePricePerDose = 10 //ledger(Drugs).bought / ledger(Drugs).paid
	      val e = Exchange(averagePricePerDose/retailers.size, Drugs)
	      self ! ExpectDeliveries(retailers)
	      retailers foreach { _ ! e }
//	      self ! "a string"
	      // ask retailers for drugs
	    // case ExpectReplies(_,_) => println("wth")
      }
	}
  } 

  case class Retailer (
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext) : Receive = {
      import context.sender
      implicit def self : ActorRef = context.self
      
      {
        case Tick(time) =>
          println(self+" is retailer at "+time)
          self ! ExpectDeliveries()
        case e @ Exchange( paid, Drugs ) =>
          sender ! e.deliver(amount=10)
        case Deliver(Drugs, amt, cashBack) =>
      }
    }
  }
  
  final case class Supplier (
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext) : Receive = {
      import context._
      {
        case Exchange( paid, Precursor ) =>
        case Deliver( Precursor, amt, cashBack ) =>
      }
	}
  }
  
  final case class Middleman (
    wholesaler:ActorRef, 
    cook:ActorRef,
    suppliers:Set[ActorRef],
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext) : Receive = {
      import context._
      {
        case Exchange( paid, Precursor ) =>
          //val Entry(available, cost, other, yetanother) = ledger(Precursor)
      }
    }
  }
  
  final case class Wholesaler (
    middleman:ActorRef, 
    retailers:Set[ActorRef],
    margin:Double
  ) extends GangMember {
    def receive()(implicit context : ActorContext) : Receive = {
      import context._
      {
        case Exchange( pay, Drugs ) =>
        case Deliver(Drugs, amt, paid) =>
      }
    }
  }
  
  final case class Cook (
    middleman:ActorRef,
    margin:Double
  ) extends GangMember {
	def receive()(implicit context : ActorContext) : Receive = {
      import context._
      {
	    case Exchange( paid, Drugs ) =>
	    case Deliver( Precursor, amt, cashBack) =>
      }
	}
  }

class GangActor extends Actor with Stash {
   
  var cashOnHand : Double = 0d;
  var net : Double = 0L;
  var Time : Long = 0L;
  
  def advance = {
    val send = Ack(Tick(Time))
    //println(self+" sending "+send)
    context.parent ! send
    Time += 1
  }
  
  import GangMember.Ledger
  val ledger : Ledger = initLedger
  

  
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
      cashOnHand -= paid
      if (awaiting.isEmpty) advance
    case d : Deliver => println("wth")
    // case m => println("wth "+m)
    
  }
  
}

case class Tick(time:Long)

class Runner extends Actor {
  //implicit val timeout = akka.util.Timeout(1000)
    
  val world, middleman, wholesaler, cook = context.actorOf(Props[GangActor])
  val retailers = Iterable.fill(5)( context.actorOf(Props[GangActor]) ).toSet
  val suppliers = Iterable.fill(5)( context.actorOf(Props[GangActor]) ).toSet
  
  val drugsWantedPerTime = 1000
  val precursorPerPaid = 10
  val middlemanMargin, wholesalerMargin, cookMargin, retailMargin, supplierMargin = 0.10
  
  // initialize the world
  world ! World(precursorPerPaid, drugsWantedPerTime, retailers)
  middleman ! Middleman(wholesaler, cook, suppliers, middlemanMargin)
  wholesaler ! Wholesaler(middleman, retailers, wholesalerMargin)
  cook ! Cook(middleman, cookMargin)
  
  retailers foreach { _ ! Retailer(wholesaler,world,retailMargin) }
  suppliers foreach { _ ! Supplier(middleman,world,supplierMargin) }
  
  //val all = (world +: middleman +: wholesaler +: cook +: (retailers ++ suppliers)).toSet
  val all : Set[ActorRef] = retailers + world
  
  def alerting(Time:Long = 0)(implicit group:Set[ActorRef]) : Receive = {
    case t @ Tick(10) => context.system.shutdown
    case t @ Tick(Time) =>
      context become awaiting(group, Time)
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
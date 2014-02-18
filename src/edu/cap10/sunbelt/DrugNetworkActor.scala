package edu.cap10.sunbelt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import akka.actor.ActorContext

case class Ack(msg:Any)
  
object Resources extends Enumeration { val Precursor, Drugs = Value }
import Resources.{Value => Resource, _}

case class Deliver(what:Resource, amount:Double, paid:Double) {
  lazy val book = Book(what, amount, paid)
}

case class Exchange(pay:Double, want:Resource) {
  def deliver(charge:Double = pay, amount:Double) = Ack(Deliver(want, amount, charge))
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

sealed trait GangMember extends Receive {
  val block : Receive
//  val rec : Receive = { 
////    case init(altL) => 
////      println("got it")
////      println(this.toString + " " +(altL == ledger))
//    case s:String => println(s)
//  }
  override def apply(msg:Any) : Unit = block(msg)
  override def isDefinedAt(msg:Any) : Boolean = msg match {
    case s:String => println("got "+s); false
    case _ => block.isDefinedAt(msg) // TODO probably way suboptimal
  }
}

import scala.util.{Try, Failure, Success}

case class ExpectReplies(from:Set[ActorRef] = Set.empty, time:Long)

class GangActor extends Actor {
   
  var cashOnHand : Double = 0d;
  var net : Double = 0L;
  var Time : Long = 0L;
  
  import GangMember.Ledger
  val ledger : Ledger = initLedger
  
  final case class World (
    precursorPerPaid:Double = 0,
    drugsWantedPerTime:Double,
    retailers : Set[ActorRef]
  ) extends GangMember {
    val block : Receive = {
//	    case Exchange( paid, Precursor ) =>
//	      val book = Book(Precursor, -paid*precursorPerPaid, paid)
//	      self ! book
//	      sender ! Ack(book.deliver)
//	      // the World has infinite Precursor, however need to book it
	    case Tick(time) if time == Time =>
	      println("world "+self+" at "+time)
	      val averagePricePerDose = 10 //ledger(Drugs).bought / ledger(Drugs).paid
	      retailers foreach { _ ! Exchange(averagePricePerDose, Drugs) }
	      self ! ExpectReplies(retailers, time)
	      self ! "a string"
	      // ask retailers for drugs
	    // case ExpectReplies(_,_) => println("wth")
	}
  } 
  
  final case class Retailer (
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double
  ) extends GangMember {
    val block : Receive = {
      case Tick(time) if time == Time =>
        println("retailer "+self+" at "+time)
        self ! ExpectReplies(time=time)
      case e @ Exchange( paid, Drugs ) =>
        sender ! e.deliver(amount=10)
      case Deliver(Drugs, amt, cashBack) =>
    }
  }
  
  final case class Supplier (
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double
  ) extends GangMember {
    val block : Receive = {
	  case Exchange( paid, Precursor ) =>
	  case Deliver( Precursor, amt, cashBack ) =>
	}
  }
  
  final case class Middleman (
    wholesaler:ActorRef, 
    cook:ActorRef,
    suppliers:Set[ActorRef],
    margin:Double
  ) extends GangMember {
    val block : Receive = {
      case Exchange( paid, Precursor ) =>
        val Entry(available, cost, other, yetanother) = ledger(Precursor)
    }
  }
  
  final case class Wholesaler (
    middleman:ActorRef, 
    retailers:Set[ActorRef],
    margin:Double
  ) extends GangMember {
    val block : Receive = {
      case Exchange( pay, Drugs ) =>
      case Deliver(Drugs, amt, paid) =>
    }
  }
  
  final case class Cook (
    middleman:ActorRef,
    margin:Double
  ) extends GangMember {
	val block : Receive = {
	  case Exchange( paid, Drugs ) =>
	  case Deliver( Precursor, amt, cashBack) =>
	}
  }
  
//  import Entities._
  
  def receive = {
    case w @ World(pp,dv,rs) =>
      context become (World(pp,dv,rs), false)
    case s @ Supplier(wo,wh,m) =>
      context become (Supplier(wo,wh,m), false)
    case r @ Retailer(mi,wh,m) =>
      context become (Retailer(mi,wh,m), false)
    case ExpectReplies(from, time) if from.isEmpty => 
      println(self+" is awaiting nothing")
      context.parent ! Ack(time)
      Time += 1
    case ExpectReplies(from, time) =>
      println(self+" is awaiting "+from)
      var awaiting = from
      val Tick = Ack(time)
      context become({
        case Tick =>
          awaiting -= sender
          if (awaiting.isEmpty) {
            context unbecome()
            context.parent ! Tick
            Time += 1
          }
        case Ack(msg) =>
          self forward msg
          self forward Tick
        case e:ExpectReplies => println(self+" wth "+e)   
      }, false)
    case d @ Deliver(what, amt, paid) =>
      println(sender +" delivered "+d)
      ledger(what) = ledger(what)+d.book
      cashOnHand -= paid
    // case m => println("wth "+m)
    
  }
  
}

import scala.concurrent.Future
import scala.concurrent.duration.Duration._
import scala.concurrent.Await
import scala.util.Success
import akka.pattern.ask

case class Tick(time:Long)

class Runner extends GangActor {
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
  
  override def receive = {
    case t @ Tick(10) => context.stop(self)
    case t @ Tick(time) if time == Time =>
      println("runner at "+time)
      all foreach { _ ! t }
      context become awaiting(all)
    case Ack(any) => println("acked "+any)
    case m => println(m)
  }
  
  def awaiting(who:Set[ActorRef]) : Receive = {
    case Ack(Tick(time)) if (time == Time) && (who contains sender) =>
      println("not awaiting "+sender)
      who - sender match {
        case s if s.isEmpty =>
          context unbecome()
          Time += 1
          self ! Tick(Time)
        case s => context become awaiting(s)
      }
    case e:ExpectReplies => println(self+" wth "+e)
  }
}
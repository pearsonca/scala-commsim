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
  def deliver(charge:Double, amount:Double) = Ack(Deliver(want, amount, charge))
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
  val rec : Receive = { 
//    case init(altL) => 
//      println("got it")
//      println(this.toString + " " +(altL == ledger))
    case s:String => println(s)
  }
  override def apply(msg:Any) : Unit = (rec orElse block)(msg)
  override def isDefinedAt(msg:Any) : Boolean = (rec orElse block).isDefinedAt(msg) // TODO probably way suboptimal
  def ledger : Ledger
}

//object Entities {
//  def become(g:GangMember)(implicit context : ActorContext, ledger:Ledger) : Unit = g match {
//    case e:World => become(e)
//    case e:Middleman => become(e)
//    case e:Wholesaler => become(e)
//    case e:Cook => become(e)
//    case e:Retailer => become(e)
//    case e:Supplier => become(e)
//  }
//  def become(e:World)(implicit context : ActorContext, ledger:Ledger) : Unit = context become (e.copy(), false)
//
//  def become(e:Middleman)(implicit context : ActorContext, ledger:Ledger) : Middleman = {
//    val cp = e.copy()
//    context become (cp, false)
//    cp
//  }
//  def become(e:Wholesaler)(implicit context : ActorContext, ledger:Ledger) : Wholesaler = {
//    val cp = e.copy()
//    context become (cp, false)
//    cp
//  }
//  def become(e:Cook)(implicit context : ActorContext, ledger:Ledger) : Cook = {
//    val cp = e.copy()
//    context become (cp, false)
//    cp
//  }
//  def become(e:Retailer)(implicit context : ActorContext, ledger:Ledger) : Retailer = {
//    val cp = e.copy()
//    context become (cp, false)
//    cp
//  }
//  def become(e:Supplier)(implicit context : ActorContext, ledger:Ledger) : Supplier = {
//    val cp = e.copy()
//    context become (cp, false)
//    cp
//  }
//}

case class World(
    precursorPerPaid:Double = 0,
    drugsWantedPerTime:Double)
    (implicit val context : ActorContext, val ledger: Ledger = MMap.empty) 
extends GangMember {
  import context.{sender, self}
  val block : Receive = {
    case Exchange( paid, Precursor ) =>
      val book = Book(Precursor, -paid*precursorPerPaid, paid)
      self ! book
      sender ! Ack(book.deliver)
      // the World has infinite Precursor, however need to book it
    case "hw" => println("hello world")
    // case Tick(Time) =>
      // ask retailers for drugs
  }
} 

case class Middleman(
    wholesaler:ActorRef, 
    cook:ActorRef,
    suppliers:Seq[ActorRef],
    margin:Double)
    (implicit val context : ActorContext, val ledger: Ledger = initLedger)
extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( paid, Precursor ) =>
      val Entry(available, cost, other, yetanother) = ledger(Precursor)
  }
}

case class Wholesaler(
    middleman:ActorRef, 
    retailers:Seq[ActorRef],
    margin:Double)
    (implicit val context : ActorContext, val ledger: Ledger = initLedger)
extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( pay, Drugs ) =>
    case Deliver(Drugs, amt, paid) =>
  }
}

case class Cook(
    middleman:ActorRef,
    margin:Double)
    (implicit val context : ActorContext, val ledger: Ledger = initLedger)
extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( paid, Drugs ) =>
    case Deliver( Precursor, amt, cashBack) =>
  }
}

case class Retailer(
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double)
    (implicit val context : ActorContext, val ledger: Ledger = initLedger)
extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( Drugs, paid ) =>
    case Deliver(Drugs, amt, cashBack) =>
  }
}

case class Supplier(
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double)
    (implicit val context : ActorContext, val ledger: Ledger = initLedger)
extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( paid, Precursor ) =>
    case Deliver( Precursor, amt, cashBack ) =>
  }
}

import scala.util.{Try, Failure, Success}

class GangActor extends Actor {
   
  implicit var cashOnHand : Double = 0L;
  import GangMember.Ledger
  implicit val ledger : Ledger = initLedger
    
  case class ExpectReplies(from:Set[ActorRef], time:Long)
  
  
  
//  import Entities._
  
  def receive = {
    case w : World =>
      context become (w.copy(), false)
    case init(_) => println("swalling init?")
    case ExpectReplies(from, time) =>
      var awaiting = from
      context become({
        case Ack(msg) =>
          awaiting -= sender
          self forward msg
          if (awaiting.isEmpty) {
            context unbecome()
            context.parent ! Ack(time)
          }
        case e:ExpectReplies => throw new UnsupportedOperationException   
      }, false)
    case d @ Deliver(what, amt, paid) =>
      ledger(what) = ledger(what)+d.book
      cashOnHand -= paid
    case m => println("wth "+m)
    
  }
  
}

import scala.concurrent.Future
import scala.concurrent.duration.Duration._
import scala.concurrent.Await
import scala.util.Success
import akka.pattern.ask

case class Tick(time:Long)

class Runner extends Actor {
  //implicit val timeout = akka.util.Timeout(1000)
  
  val world, middleman, wholesaler, cook = context.actorOf(Props[GangActor])
  val retailers = Iterable.fill(5)( context.actorOf(Props[GangActor]) ).toSeq
  val suppliers = Iterable.fill(5)( context.actorOf(Props[GangActor]) ).toSeq

  val drugsWantedPerTime = 1000
  val precursorPerPaid = 10
  val middlemanMargin, wholesalerMargin, cookMargin, retailMargin, supplierMargin = 0.10
  
  // initialize the world
  world ! World(precursorPerPaid, drugsWantedPerTime)
  middleman ! Middleman(wholesaler, cook, suppliers, middlemanMargin)
  wholesaler ! Wholesaler(middleman, retailers, wholesalerMargin)
  cook ! Cook(middleman, cookMargin)
  
  retailers foreach { _ ! Retailer(wholesaler,world,retailMargin) }
  suppliers foreach { _ ! Supplier(middleman,world,supplierMargin) }
  
  val all = (world +: middleman +: wholesaler +: cook +: (retailers ++ suppliers)).toSet
  
  def receive = {
    case t @ Tick(time) => 
      all foreach { _ ! t }
      context become awaiting(all)(time)
    case Ack(any) => println("acked "+any)
    case m => println(m)
  }
  
  def awaiting(who:Set[ActorRef])(implicit Time : Long) : Receive = {
    case Ack(Tick(Time)) if who contains sender =>
      who - sender match {
        case s if s.isEmpty =>
          context unbecome()
          self ! Tick(Time+1)
        case s => context become awaiting(s)
      }
  }
}
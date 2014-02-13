package edu.cap10.sunbelt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import akka.actor.ActorContext

// participants have a few basic characteristics
//  - job queue size
//  - price
//  - quality of product
//  - rate of work

import scala.collection.immutable.Queue

case class Ack(msg:Any)
  
object Resources extends Enumeration { val Cash, Precursor, Drugs = Value }
import Resources.{Value => Resource, _}
case class Exchange(give:(Resource,Double), get:(Resource,Double))

trait GangMember extends Receive {
  val block : Receive
  override def apply(msg:Any) : Unit = block(msg)
  override def isDefinedAt(msg:Any) : Boolean = block.isDefinedAt(msg) // TODO probably way suboptimal
}

case class ChangePrecursorPerPaid(newVal:Double)
case class World(precursorPerPaid:Double = 0)(implicit context : ActorContext) extends GangMember {
  import context.sender
  val block : Receive = {
    case ChangePrecursorPerPaid(newVal) => context become copy(precursorPerPaid = newVal)
    case Exchange( (Cash,paid), (Precursor,want) ) =>
      sender ! (Precursor, paid*precursorPerPaid)
  }
} 

case class Middleman(
    wholesaler:ActorRef, 
    cook:ActorRef,
    suppliers:Seq[ActorRef],
    margin:Double)(implicit context : ActorContext) extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( (Cash,paid), (Precursor,want) ) =>    
  }
}

case class Wholesaler(
    middleman:ActorRef, 
    retailers:Seq[ActorRef],
    margin:Double)(implicit context : ActorContext) extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( (Cash,paid), (Precursor,want) ) =>    
  }
}

case class Cook(
    middleman:ActorRef,
    margin:Double)(implicit context : ActorContext) extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( (Cash,paid), (Precursor,want) ) =>    
  }
}

case class Retailer(
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double)(implicit context : ActorContext) extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( (Cash,paid), (Precursor,want) ) =>    
  }
}

case class Supplier(
    wholesaler:ActorRef, 
    world:ActorRef,
    margin:Double)(implicit context : ActorContext) extends GangMember {
  import context.sender
  val block : Receive = {
    case Exchange( (Cash,paid), (Precursor,want) ) =>    
  }
}

class GangActor extends Actor {

  case class GetPrecursor(paid:Double)
  
  var supplies = Map.empty[Resource, Double]



  case class ExpectReplies(from:Set[ActorRef], time:Long)
  
  var awaiting = Set.empty[ActorRef]
  
  def receive = {
    case gang : GangMember =>
      context become(gang,false)
      sender ! Ack(gang)
    case ExpectReplies(from, time) =>
      awaiting ++= from
      context become({
        case Ack(msg) =>
          awaiting -= sender
          self forward msg
          if (awaiting.isEmpty) {
            context unbecome()
            context.parent ! Ack(time)
          }
      }, false)
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

  val drugVal = 1000
  val precursorPerPaid = 10
  val middlemanMargin, wholesalerMargin, cookMargin, retailMargin, supplierMargin = 0.10
  
  // initialize the world
  world ! World(precursorPerPaid)
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
  }
  
  def awaiting(who:Set[ActorRef])(implicit Time : Long) : Receive = {
    case Ack(Tick(Time)) if who contains sender =>
      who - sender match {
        case s if s.isEmpty => context unbecome
        case s => context become awaiting(s)
      }
  }
}
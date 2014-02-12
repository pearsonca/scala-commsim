package edu.cap10.sunbelt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props

// participants have a few basic characteristics
//  - job queue size
//  - price
//  - quality of product
//  - rate of work

import scala.collection.immutable.Queue

class GangActor extends Actor {

  case class Ack(msg:Any)
  
  object Resources extends Enumeration { val Cash, Precursor, Drugs = Value }
  import Resources.{Value => Resource, _}
  case class Exchange(give:(Resource,Double), get:(Resource,Double))
  
  case class GetPrecursor(paid:Double)
  
  var supplies = Map.empty[Resource, Double]
  
  case class World(precursorPerPaid:Double = 0) extends Receive {
    override def apply : Receive = {
      case ChangePrecursorPerPaid(newVal) => context become copy(precursorPerPaid = newVal)
      case Exchange( (Cash,paid), (Precursor,want) ) =>
        sender ! (Precursor, paid*precursorPerPaid)
	}
  } 

  case class ChangePrecursorPerPaid(newVal:Double)

  case class ExpectReplies(from:Set[ActorRef], time:Long)
  
  var awaiting = Set.empty[ActorRef]
  
  def receive = {
    case world : World => context become(world,false)
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
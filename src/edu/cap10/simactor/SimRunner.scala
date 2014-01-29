package edu.cap10.simactor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorContext

object SimRunner {
  case class Time(t:Long)
  case class TimeAck(t:Long)
}

class SimRunner extends Actor {
  import SimRunner._
  
  
  def receive = {
    case TimeAck(t) =>
  }
  
}

object AckHelp {
  case class ExpectFrom(senders:Set[ActorRef])
  object DoneAck
  object Ack
  def props(): Props = Props(classOf[AckHelp])
}

class AckHelp extends Actor {
  import AckHelp._
  import context._
  def receive = {
    case ExpectFrom(senders) => become( expecting(senders) )
    case _ => // do nothing
  }
  
  def expecting(senders:Set[ActorRef]) : Receive = {
    case Ack if (senders(sender)) => 
      senders - sender match {
        case leftovers if leftovers.isEmpty => 
          parent ! DoneAck
          unbecome
        case leftovers => become ( expecting(leftovers) )
      }
      
    case ExpectFrom(moreSenders) =>
      become ( expecting(senders ++ moreSenders) )
  }
  
}

trait BaseSimActor {
  implicit val context : ActorContext
  import context._
  import AckHelp._
  import Actor.Receive
  import SimRunner._
  
  val ackHelper = actorOf(AckHelp.props)
  
  def simBase(step: =>Unit, time: =>Long) : Receive = {
    case a @ Ack => ackHelper forward a
    // ackHelper manages whether all my messages have been received
  	case DoneAck =>
  	// when it tells me they are, notify the simulation runner (parent)
      parent ! TimeAck(time)
      step
  }
  
  def awaiting(who:Set[ActorRef]) = ackHelper ! ExpectFrom(who)
  def ack = sender ! Ack
}
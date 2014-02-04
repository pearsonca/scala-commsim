package edu.cap10.simactor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorContext
import akka.actor.ActorSystem

object Simulation extends App {
  val system = ActorSystem()
  // TODO parse args
  val runner = system.actorOf[SimRunner] 
}

object SimRunner {
  case class Start(duration:Long)
  case class Clock(left:Long,now:Long=0) {
    def unary_+ = Clock(left-1,now+1)
    lazy val done = left == 0
  }
  
  case class Time(t:Long)
  case class TimeAck(t:Long) {
    lazy val next = Time(t+1)
  }
  case class Create(props:Props, initMsgs:Seq[Any])
  case class CreateBlock(blocks:Map[Props,(Int,Seq[Any])])
  def props(): Props = Props(classOf[SimRunner])
}

class SimRunner extends Actor {
  import SimRunner._
  import context.{ become, actorOf => create }
  // 
  
  def init(c:Any) = {
    become(initial())
    self ! c
  }
  
  def receive = {
    case m => init(m)
  }
  
  def initial(members : Set[ActorRef] = Set.empty) : Receive = {
    case Create(props, msgs) =>
      val newMember = create(props)
      msgs foreach { newMember ! _}
    case CreateBlock(blocks) =>
      blocks foreach {
        case (props,(count,msgs)) =>
          val send = Create(props,msgs)
          for (i <- 0 until count) self ! send
      }
    case Start(duration) => 
      become(running(members)( members,Clock(duration)) )
      val go = Time(0)
      members foreach { _ ! go }
  }
  
  def running(awaiting:Set[ActorRef] = Set.empty)(implicit members:Set[ActorRef], clock:Clock) : Receive = {
	import clock._
    
    {
      case time @ TimeAck(t) if t == now => 
        awaiting - sender match {
          case done if done.isEmpty =>
            become(running()(members,+clock))
            members foreach { _ ! time.next }
          case left => become(running(left))
        }
      case Create(props, msgs) => 
        val newMember = create(props)
        msgs foreach { newMember ! _ }
        become( running(awaiting)(members + newMember,clock) )
    }
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
package edu.cap10.simactor

import akka.actor.Actor
import akka.actor.Actor.Receive
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
  case class Start(duration:Time)
  case class Clock(left:Time, now:Time=0) {
    def unary_+ = Clock(left-1,now+1)
    lazy val done = left == 0
  }
  
  case class Tick(t:Time) { lazy val done = TickAck(t) }
  case class TickAck(t:Time) { lazy val next = Tick(t+1) }
  val Go = Tick(0)
  
  case class Create(props:Props, initMsgs:Seq[Any])
  case class CreateBlock(blocks:Map[Props,(Int,Seq[Any])])
  def props(): Props = Props(classOf[SimRunner])
}

class SimRunner extends Actor {
  import SimRunner._
  import context.{ become, actorOf => create }
  // 
    
  def receive = initial()
  
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
      members foreach { _ ! Go }
  }
  
  def running(awaiting:Set[ActorRef] = Set.empty)(implicit members:Set[ActorRef], clock:Clock) : Receive = {
	import clock._
    
    {
      case time @ TickAck(t) if t == now => 
        awaiting - sender match {
          case done if done.isEmpty =>
            become(running()(members,+clock))
            members foreach { _ ! time.next }
          case left => become(running(left))
        }
      // not currently handling expanding membership as sim proceeds
      // e.g., births
    }
  }

}

trait Handler {
  implicit val context : ActorContext
  private val noop : Receive = { case _ => }
  lazy val receive : Receive = handle()

  def handlers : List[Receive] = Nil
  def handle(chain: => List[Receive] = handlers) : Receive = {
    case m => chain.find( _.isDefinedAt(m) ).getOrElse(noop).apply(m)
  }
  
}

trait AckHandler extends Handler {
  import context._
  import SimRunner._
  
  private var expecting : People = empty
  private var t : Time = 0
  
  def timeStep = { t = t+1 }
  def ack = sender ! Ack
  
  case class ExpectFrom(senders:Set[ActorRef])
  object DoneAck
  object Ack
  
  private val expect : Receive = {
    case Ack if expecting contains sender => 
      expecting = expecting - sender
      if (expecting.isEmpty) self ! DoneAck
    case ExpectFrom(moreSenders) => expecting = expecting ++ moreSenders
    case DoneAck =>
      parent ! TickAck(t)
      timeStep
  }
  
  override def handlers = expect :: super.handlers
}
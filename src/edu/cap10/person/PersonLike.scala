package edu.cap10.person

import scala.collection._
import scala.collection.mutable.{Buffer => MBuffer}
import scala.actors.Actor

import edu.cap10.message.Message
import Community.{Value => CValue}
import Vocabulary.{Value => VValue}

trait PersonLike extends Actor {
	def contacts : Map[CValue, MBuffer[PersonLike]]
	def join(other:PersonLike, commType:Community.Value) = {
    	other.contacts(commType) += this
    	contacts(commType) += other
	}

	/** 
	 * The monitoring method.  Default monitoring is to System.out.
	 * 
	 * monitor(msg) is called for messages received into update(msg).
	 * 
	 * @return the incoming message without any side-effects
	 */
	def monitor(msg: Message) = {
	  println(id + ", "+msg)
	  msg
	}
	
	/** 
	 *  This is a hook for making any internal state changes to a person (which, e.g.,
	 *  might affect messages()).  The default behavior clears the inbox
	 */
	def update(t:Int) : Unit = inbox.clear
	
	/** 
	 *  This is a hook for prepare for internal state changes to a person (which, e.g.,
	 *  might affect update()).  It's default behavior adds the msg to its inbox 
	 */
	def update(msg:Message) : Unit = inbox += monitor(msg)
	val inbox = MBuffer[Message]()
	
	/** 
	 *  This is the algorithm for deciding what messages will be sent on an iteration.
	 *  
	 *  @return a map by community type to an iterable of who-what pairs.  The community type
	 *  sets which contacts entry to bring up, then the who index sets which person to send to.
	 *  Message content is set by the what (Vocabulary) part of the pair.
	 *  */
	def messages() : Map[CValue, Iterable[(PersonLike,VValue)]]
	
	def id() : Int
	override def hashCode = id
	
	def testEvent(t:Int) = println(id + " received TEST @ "+t)
	
	def act() = loop {
	 react {
	    case SimulationCommand(SimulationEvent.NEXT, t) =>
	      update(t)
	      sendMessages(messages)
	      reply("ACK")
	    case SimulationCommand(SimulationEvent.TEST, t) => 
	    	testEvent(t)
	    case m:Message => update(m)
	    case SimulationCommand(SimulationEvent.DONE, t) => stop
	  }
	}
	
	def stop = exit
	
	def messenger(community:CValue, what:VValue) = Message(this,community,what)
	
	def sendMessages(msgs:Map[CValue,Iterable[(PersonLike,VValue)]]) = 
	  for (community <- msgs.keys; 
		recipients = contacts(community); 
	    (who,what) <- msgs(community)) {
	        who ! messenger(community, what)
	  }
	
}

object Community extends Enumeration {
  val Religion, Work, Family, Plot = Value
}

object Vocabulary extends Enumeration {
  val Good, Bad = Value;
}

object SimulationEvent extends Enumeration {
  val NEXT, DONE, TEST = Value
}

case class SimulationCommand(e:SimulationEvent.Value = SimulationEvent.NEXT,t:Int)
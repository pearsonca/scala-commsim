package edu.cap10.person

import scala.collection._
import scala.collection.mutable.{Buffer => MBuffer}
import scala.actors.Actor

import edu.cap10.message.Message
import Community.{Value => CValue}
import Vocabulary.{Value => VValue}

import edu.cap10.app.Logger

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
	  Logger.println(this,id+" "+msg)
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
	def messages() : Map[CValue, Iterable[(PersonLike,VValue)]] = { 
	  for ( community <- contacts.keys; // for each of the PersonLike's communities
			res = messages(community);	// generate messages for that community
			if res.size != 0)			// if there are messages
	    yield community -> res			// add them to the outbox
	}.toMap
	
	def messages(commType:CValue) : Iterable[(PersonLike,VValue)]
	
	def id() : Int
	override def hashCode = id
	
	def testEvent(t:Int) = println(id + " received TEST @ "+t)
	
	def act() = loop {
	 react {
	   	case SimulationCommand(SimulationEvent.UPDATE, t) =>
	      update(t)
	      reply("ACK")
	    case SimulationCommand(SimulationEvent.NEXT, t) =>
	      sendMessages(messages, t)
	      reply("ACK")
	    case SimulationCommand(SimulationEvent.TEST, t) => 
	    	testEvent(t)
	    case m:Message => update(m)
	    case SimulationCommand(SimulationEvent.DONE, t) => stop
	  }
	}
	
	def stop = exit
	
	def messenger(community:CValue, what:VValue, t:Int) = Message(this,community,what,t)
	
	def sendMessages(msgs:Map[CValue,Iterable[(PersonLike,VValue)]], t:Int) = 
	  for (community <- msgs.keys; 
	    (who,what) <- msgs(community)) {
	        who ! messenger(community, what, t)
	  }
	
}

object Community extends Enumeration {
  val Religion, Work, Family, Plot = Value
}

object Vocabulary extends Enumeration {
  val Good, Bad = Value;
}

object SimulationEvent extends Enumeration {
  val UPDATE, NEXT, DONE, TEST = Value
}

case class SimulationCommand(e:SimulationEvent.Value = SimulationEvent.NEXT,t:Int)

object SimulationCommand {
  def apply(e:SimulationEvent.Value) : (Int) => SimulationCommand = SimulationCommand(e,_)
}
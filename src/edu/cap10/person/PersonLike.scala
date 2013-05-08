package edu.cap10.person

import scala.collection._
import scala.collection.mutable.{Buffer => MBuffer}
import scala.actors._
import Community.{Value => CommunityType}

trait PersonLike extends Actor {
	def contacts : Map[CommunityType, MBuffer[PersonLike]]
		
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
	def update() : Unit = inbox.clear
	
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
	def messages() : Map[CommunityType, Iterable[(PersonLike,Vocabulary)]]
	
	def id() : Int
	override def hashCode = id
	override def equals(other:Any) = this eq other
	
	def messenger(community:CommunityType, what:Vocabulary) = Message(this,community,what)
	
	def sendMessages(msgs:Map[CommunityType,Iterable[(PersonLike,Vocabulary)]]) = 
	  for (community <- msgs.keys; 
		recipients = contacts(community); 
	    (who,what) <- msgs(community)) {
	        who ! messenger(community, what)
	  }
	
	def act() = loop {
	  react {
	    case "NEXT" =>
	      update
	      sendMessages(messages)
	      reply("ACK")  
	    case m:Message => update(m)
	  }
	}
	
}

object Community extends Enumeration {
  val Religion, Work, Family, Plot = Value
}

sealed trait Vocabulary
case object Good extends Vocabulary {
  override val toString = 0.toString
}
case object Bad extends Vocabulary {
  override val toString = 1.toString
}

case class Message(sender:PersonLike, community:CommunityType, content:Vocabulary) {
  override val toString = sender.id +", "+community+", "+content
}
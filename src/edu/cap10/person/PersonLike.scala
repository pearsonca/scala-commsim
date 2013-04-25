package edu.cap10.person

import scala.collection._
import scala.collection.mutable.{Buffer => MBuffer}
import scala.actors._

trait PersonLike extends Actor {
	val contacts : Map[CommunityType,MBuffer[PersonLike]]
	
	def connect(community:CommunityType, people:PersonLike*) = {
	  contacts(community) ++= people
	  this
	} 
	
	def monitor(msg: Message) : Unit = {
	  println(id + ", "+msg)
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
	def update(msg:Message) : Unit = inbox += msg
	val inbox = MBuffer[Message]()
	
	/** 
	 *  This is the algorithm for deciding what messages will be sent on an iteration.
	 *  
	 *  @return a map by community type to an iterable of who-what pairs.  The community type
	 *  sets which contacts entry to bring up, then the who index sets which person to send to.
	 *  Message content is set by the what (Vocabulary) part of the pair.
	 *  */
	def messages() : Map[CommunityType,Iterable[(Int,Vocabulary)]]
	
	def id() : String
	def messenger(community:CommunityType, what:Vocabulary) = Message(this,community,what)
	
	def sendMessages(msgs:Map[CommunityType,Iterable[(Int,Vocabulary)]]) = 
	  for (community <- msgs.keys; 
		recipients = contacts(community); 
	    (who,what) <- msgs(community)) {
	        recipients(who) ! messenger(community,what)
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

sealed trait CommunityType
case object Religion extends CommunityType {
  override val toString = "RELIGION"
}
case object Work extends CommunityType {
  override val toString = "WORK"
}
case object Family extends CommunityType {
  override val toString = "FAMILY"
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
class CommLine
package edu.cap10.person

import scala.collection._
import scala.collection.mutable.{Buffer => MBuffer}
import scala.actors.Actor
import scala.collection.immutable.Stream.{continually => fill};

import Community.{Value => CValue}
import Vocabulary.{Value => VValue}

import edu.cap10.app.Logger

import edu.cap10.sim.SimulationActor
import edu.cap10.sim.Event
import edu.cap10.graph.Vertex

trait PersonLike extends Vertex[Community.Value,PersonLike] with SimulationActor[Long,(Community.Value, Vocabulary.Value, PersonLike)] {
    type Repr = PersonLike
  	
	val logger = new Logger {
	  def record(msg:MType, t:Int) = {
	    // println(id+" "+msg+" ")
	    msg
	  }
	}
	
	/** 
	 *  This is a hook for making any internal state changes to a person (which, e.g.,
	 *  might affect messages()).  The default behavior clears the inbox
	 */
	val inbox = MBuffer[MType]()
	override def hear(msg:MType,t:Int) = {
	  inbox += super.hear(msg,t)
	  msg
	}
	override def update(t:Int) = {
	  inbox.clear
	  super.update(t)
	}
	override def next(t:Int) = {
	  val msgs = messages()
	  for (community <- msgs.keys; 
	    (who,what) <- msgs(community)) {
	        who ! messenger(community, what, t)
	  }
	  super.next(t)
	}
	
	/** 
	 *  This is the algorithm for deciding what messages will be sent on an iteration.
	 *  
	 *  @return a map by community type to an iterable of who-what pairs.  The community type
	 *  sets which contacts entry to bring up, then the who index sets which person to send to.
	 *  Message content is set by the what (Vocabulary) part of the pair.
	 *  */
	def messages() : Map[CValue, Iterable[(PersonLike,VValue)]] = { 
	  for ( community <- edgeTypes; // for each of the PersonLike's communities
			res = messages(community);	// generate messages for that community
			if res.size != 0)			// if there are messages
	    yield community -> res			// add them to the outbox
	}.toMap
	
	def messages(commType:CValue) : Iterable[(PersonLike,VValue)]
	
	import edu.cap10.sim.EventType
	def messenger(community:CValue, what:VValue, t:Int) = Event(EventType.MSG,t,(community,what,this))
	
}

object Community extends Enumeration {
  val Religion, Work, Family, Plot = Value
}

object Vocabulary extends Enumeration {
  val Good, Bad = Value;
}

//object SimulationEvent extends Enumeration {
//  val UPDATE, NEXT, DONE, TEST = Value
//}
//
//case class SimulationCommand(e:SimulationEvent.Value = SimulationEvent.NEXT,t:Int)
//
//object SimulationCommand {
//  def apply(e:SimulationEvent.Value) : (Int) => SimulationCommand =
//    SimulationCommand(e,_)
//}
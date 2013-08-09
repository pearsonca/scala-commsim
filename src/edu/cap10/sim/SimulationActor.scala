package edu.cap10.sim

import scala.actors.Actor

/** Provides a trait to implement a simple, discrete-events-at-discrete-times, agent-based simulation.
 *  @parameter ID the type of ID for this actor (e.g., Int for numeric ids, String for names)
 *  @parameter MessageContent the type of object to be passed as message content during the simulation
 * 
 */

case class Event[MessageContent >: Null](e:EventType.Value = EventType.NEXT, t:Int, msg : MessageContent = null)

trait SimulationActor[ID, MessageContent >: Null] extends Actor {
	type MType = MessageContent
	trait Logger { def record(m:MType) : MType }
	
	
	import EventType._
  	override def act = loop {
	 react {
	   	case m:Event[MType] => {
	   	  m.e match {
	   	    case MSG => hear(m.msg,m.t)
	   	    case UPDATE => update(m.t)
		   	case NEXT => next(m.t)
		   	case DONE => done(m.t) 
		   	case TEST => test(m.t)
		  }
	    }
	   	case ACK =>
	   	case other => println(this + " Error: "+ other)
	  }
	}
  
	def id : ID
	def hear(msg:MType, t:Int) : MType = logger.record(msg)
	
	def update(t:Int) = { ack; this }
	def next(t:Int) = { ack; this }
	def done(t:Int = -1) : Nothing = exit
	
	def ack = reply(ACK)
	
	def test(t:Int) = println(id + " received TEST @ "+t)
	
	def logger : Logger
}

object SimulationActor {
  def apply[MessageContent >: Null](e:EventType.Value) = (t:Int) => Event[MessageContent](e,t,null)
}

object EventType extends Enumeration { val UPDATE, NEXT, DONE, MSG, ACK, TEST = Value }
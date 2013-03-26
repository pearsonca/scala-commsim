package edu.cap10.channels

import edu.cap10.person.Person
import edu.cap10.message._
import edu.cap10.clock._
import scala.actors._
import scala.actors.Actor._


class Path(val tar: Person, var logger: edu.cap10.message.Logger) extends Actor {
	def this(tar:Person) = this(tar,NoOpLogger)
	def setLogger(log : edu.cap10.message.Logger) = {
	  logger = log
	  this
	}
	
	override def toString = "to_" + tar
	override def hashCode = tar.id.hashCode
	override def equals(that:Any) = that match {
	  case other:Path => tar.id == other.tar.id
	  case _ => false
	}
	
	def act() = {
		loop {
			react {
			  case m : Message =>
			    logger log m
			    tar ! m
			  case msg =>
			    println("Unhandled message "+msg)
			}
		}
	}
}

object NoOpLogger extends edu.cap10.message.Logger {
  override def log(msg:Message) = {}
  override def shutdown = true
}

object Path {
  def apply(tar:Person,logger:edu.cap10.message.Logger) = new Path(tar,logger)
  def apply(tar:Person) = new Path(tar)
}
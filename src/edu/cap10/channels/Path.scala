package edu.cap10.channels

import edu.cap10.person.Person
import edu.cap10.message.Message
import edu.cap10.message.Logger
import edu.cap10.message.DefaultLogger
import scala.actors._
import scala.actors.Actor._


class Path(val tar: Person, logger: Logger) extends Actor {
	override def toString = " ->" + tar
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

object Path {
  def apply(tar:Person,logger:Logger) = new Path(tar,logger)
}
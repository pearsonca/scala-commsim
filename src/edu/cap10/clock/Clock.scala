package edu.cap10.clock

import scala.actors._
import scala.actors.Actor._
import scala.collection._
import scala.collection.mutable.BitSet

class Clock(size:Int, listener: Actor) extends Actor {
	val done = BitSet(size)
	val ready = BitSet(size)
	var time = 0
	def act = loop {
	    react {
	      case Done(i) if i < size =>
	        done += i
	        if (done.size == size) {
	            time += 1
	            done.clear
	            listener ! "COMMUNICATE"
	        }
	      case Ready(i) if i < size =>
	        ready += i
	        if (ready.size == size) {
	            ready.clear
	            listener ! "UPDATE"
	        }
	      case "STOP" => exit()
	      case msg => println("Unhandled message "+msg)
	    }
	}
}

object Clock {
  def apply(size:Int, listener:Actor) = new Clock(size,listener)
}

case class Ready(i:Int)
case class Done(i:Int)
package edu.cap10.clock

import scala.actors._
import scala.actors.Actor._
import scala.collection._
import scala.collection.mutable.BitSet

class Clock(size:Int, listener: Actor) extends Actor {
	val ref = BitSet(size)
	var time = 0
	def act() {
	    react {
	      case Done(i) if i < size =>
	        ref += i
	        if (ref.size == size) {
	            time += 1
	            ref.clear()
	            listener ! "NEXT"
	        }
	        act()
	      case "STOP" => println("shutting down Clock.")
	      case msg => println("Unhandled message "+msg) // die from unhandled message
	    }
	}
}

object Clock {
  def apply(size:Int, listener:Actor) = new Clock(size,listener)
}

case class Done(i:Int)
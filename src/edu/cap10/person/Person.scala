package edu.cap10.person

import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.{Set => MSet};
import scala.collection.mutable._;

import edu.cap10.channels.Path
import edu.cap10.message._
import edu.cap10.sim._
import edu.cap10.clock._
import edu.cap10.utils._

class Person(val id: Int) extends PersonLike {
  def messages() = {
    Map[CommunityType,Buffer[(Int,Vocabulary)]]()
  }
}

object Person {
	def apply(id:Int) = new Person(id)
}

class Plotter(id : Int) extends Person(id) {
	override def act = {
	  loop {
	    
	  }
	}
}

//class TestPerson extends Actor {
//  val others : Seq[Path];
//  def act() = {
//    loop {
//      react {
//        case msg => println("received")
//      }
//    }
//  }
//}
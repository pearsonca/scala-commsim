package edu.cap10.person

import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.Set;
import scala.collection.mutable.HashSet;

import edu.cap10.channels.Path
import edu.cap10.message._
import edu.cap10.sim._
import edu.cap10.clock._
import edu.cap10.utils._

class Person(val id: Int, val channels : Set[Path]) extends Actor with DelegateSet[Person,Path] {
	def this(id:Int) = this(id, HashSet[Path]())
	override def hashCode = id.hashCode
	override def equals(that:Any) = that match {
	  case other:Person => id == other.id
	  case _ => false
	}
		
	def mkPath(to:Person) = this + Path(to, DefaultLogger(this,to))
	
	val name : String = "P"+id
	override def toString = name
	def act = {
	  loop {
	    react {
	    	case SimTask("START", _) => {
		      this foreach { _.start }
		      act
		    }
		    case SimTask("COMMUNICATE",c) => {
		      // generate messages
		      // drop them into paths
		      // wait for ACKS?
		      c ! Done(id)
		    }
		    case SimTask("UPDATE",c) => println(name +" received UPDATE")
		    case msg =>
	    }
	  }
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
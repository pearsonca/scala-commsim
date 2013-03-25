package edu.cap10.person

import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.Set;
import scala.collection.mutable.HashSet;

import edu.cap10.channels.Path
import edu.cap10.message._


class Person(val id: Long, val channels : Set[Path]) extends Actor with scala.collection.Set[Path] {
	def this(id:Long) = this(id, HashSet[Path]())
	override def hashCode = id.hashCode
	override def equals(that:Any) = that match {
	  case other:Person => id == other.id
	  case _ => false
	}
	
	override def +(p:Path) = {
	  channels += p
	  this
	}
	
	override def -(p:Path) = {
	  channels -= p
	  this
	}
	
	override def contains(p:Path) = channels contains p
	override def iterator = channels iterator
	
	override def empty : Person = {
	  channels.empty
	  this
	}
	
	override def size = channels size
	override def foreach[U](f: Path => U) = channels foreach f
	
	def mkPath(to:Person) = this + Path(to, DefaultLogger(this,to))
	
	val name : String = "P"+id
	override def toString = name
	def act = {
	  loop {
	    
	  }
	}
}

object Person {
	def apply(id:Long) = new Person(id)
}

class Plotter(id : Long) extends Person(id) {
	override def act = {
	  loop {
	    
	  }
	}
}
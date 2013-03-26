package edu.cap10.utils

import scala.collection.mutable.Set

trait DelegateSet[This <: Set[T],T] extends Set[T] {
	val delegate = Set[T]()
	
	override def +=(t:T) = {
	  delegate += t
	  this
	}
	
	override def -=(t:T) = {
	  delegate -= t
	  this
	}
	
	override def contains(t:T) = delegate contains t
	override def iterator = delegate iterator
	
	override def empty : This = {
	  delegate.empty
	  this.asInstanceOf[This]
	}
	
	override def size = delegate size
	override def foreach[U](f: T => U) = delegate foreach f

}
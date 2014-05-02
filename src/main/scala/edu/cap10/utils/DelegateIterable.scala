package edu.cap10.utils

import scala.collection.Iterable

trait DelegateIterable[This <: Iterable[T],T] extends Iterable[T] {
	def delegate : Iterable[T]
	
	override def iterator = delegate iterator
	
	override def foreach[U](f: T => U) = delegate foreach f

	def fluenteach[U](f: T => U) : This = {
	  foreach(f)
	  this.asInstanceOf[This]
	}
	
}
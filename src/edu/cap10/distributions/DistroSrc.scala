package edu.cap10.distributions

trait DistroSrc[NumericType] {
	def inner : Iterator[NumericType]
	
	def apply(n : Int) = inner.take(n)
	def list(n : Int) = apply(n).toList
	def next = inner.next
}
package edu.cap10.distributions

trait DistroSrc[NumericType] {
	def apply(n : Int) : Iterator[NumericType]
	def list(n : Int) = apply(n).toList
	def next : NumericType
}

trait DistroSrcLike[NumericType] extends DistroSrc[NumericType] {
	def inner : Iterator[NumericType]
	def apply(n : Int) = inner.take(n)
	def next = inner.next
}
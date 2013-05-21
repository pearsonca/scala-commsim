package edu.cap10

package object distributions {
	implicit def seqToEnhanced[T](from:Seq[T]) = new RandomDrawSeq[T](from)
	class RandomDrawSeq[T](src:Seq[T]) {
	  def random() : T = src(IntRangeSrcCache(src.size).next)
	  def random( filter:(T)=>Boolean ) : T = {
	    var res = random()
	    while (!filter(res)) res = random()
	    res
	  }
	}
}
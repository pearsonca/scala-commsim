package edu.cap10

import scala.collection.SortedSet

package object distributions {
	
	implicit class RandomDrawSeq[T](src:Seq[T]) {
	  def random() : T = src(IntRangeSrcCache(src.size).next)
	  def random( filter:(T)=>Boolean ) : T = {
	    var res = random()
	    while (!filter(res)) res = random()
	    res
	  }
	}
	
	implicit class RandomDrawIterable[T](src:Iterable[T]) {
	  def random() : T = {
	    val from = IntRangeSrcCache(src.size).next
	    src.slice(from, from+1).head
	  }
	  def random( filter:(T)=>Boolean ) : T = {
	    var res = random()
	    while (!filter(res)) res = random()
	    res
	  }
	}
	
}
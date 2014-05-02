package edu.cap10

package object utils {
  
  implicit final class IndexedPairable[T](src:IndexedSeq[T]) {
	def orderedUPairs : Seq[(T,T)] =
	  for (i <- 0 until (src.length-1); l = src(i); j <- i+1 until src.length) yield (l,src(j))
  }
  
  import scala.annotation.tailrec
  
  implicit final class SeqPairable[T](src:Seq[T]) {
     def oUPairs = inner(src)
	 @tailrec private final def inner(lsrc:Seq[T], acc:Seq[(T,T)] = Nil) : Seq[(T,T)] =
      if (lsrc.isEmpty) acc.reverse else
        inner(lsrc.tail, { for (t <- lsrc.tail.reverse) yield (lsrc.head, t) } ++ acc)
  }
  
  implicit final class IterablePairable[T](src:Iterable[T]) {
    def uPairs : Iterable[(T,T)] = {
      @tailrec def inner(lsrc:Iterable[T] = src, acc:Iterable[(T,T)] = Nil) : Iterable[(T,T)] =
      if (lsrc.isEmpty) acc else
        inner(lsrc.tail, { for (t <- lsrc.tail) yield (lsrc.head, t) } ++ acc)
      inner()
    }
        
    def dPairs : Iterable[(T,T)] =
      for (left <- src; right <- src if right != left) 
    	yield (left,right)
  }
  
  import scala.collection.TraversableLike
  implicit final class FluentTraversable[T,Repr](src:TraversableLike[T,Repr]) {
    def floweach[U](f:(T)=>U) : Repr = {
      src foreach f
      src.repr
    }
    def flow[U](f:(Repr) => U) : Repr = {
      f(src.repr)
      src.repr
    }
  }
  
  final def warning(test:Boolean, msg: => Any) = if (!test) System.err.print("warning: "+msg)
  
}
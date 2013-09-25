package edu.cap10.graph.generators

import scala.collection.Iterable
import edu.cap10.graph.Vertex

object Tree {
  val DEF_WIDTH = 3
}

import Tree._
import math.log
import annotation.tailrec
import math.pow

case class Tree[EdgeType](implicit val e:EdgeType) extends Generator[EdgeType, Int] {

  	override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Seq[V], Int)) = {
  	  val (iter, width) = data
  	  iter size match {
  	    case 0 => throw new IllegalArgumentException("Attempted to tree an empty src.")
  	    case 1 => Seq(iter.head)
  	    case n => {
  	      val depth = (log(1+n*(width-1))/log(width)).toInt - 1 // inversion of geometric series sum
  	      val split = (1-pow(width,(depth+1)).toInt)/(1-width)
  	      val (treeparts, excess) = iter splitAt split
  	      val (bottom, rest) = treeparts splitAt( pow(width,depth).toInt )
  	      pile(bottom, rest, width)
  	      val excessIter = excess.iterator
  	      while (excessIter.hasNext) 
  	        for (leaf <- bottom if excessIter.hasNext) 
  	          leaf <~> excessIter.next
  	    }
  	  }
  	  iter
  	}

  	override implicit def default
	[V <: Vertex[EdgeType,V]]
	(pIter: Seq[V]) = 
      (pIter,DEF_WIDTH)

  	
  	private def bind
  	[V <: Vertex[EdgeType,V]] = 
  	  { (group:Iterable[V], head:V) => head <~> group }.tupled
  	
  	@tailrec private def pile
  	[V <: Vertex[EdgeType,V]]
  	(prevLvl : Iterable[V], unlinked : Iterable[V], width:Int = DEF_WIDTH)
	: Unit =
  	  prevLvl.size match {
  	    case 1 => Unit // done
  	    case widthPow => {
  	      val (newLvl, newUnlinked) = unlinked splitAt(widthPow / width)
  	      prevLvl.grouped(width).zip( newLvl.iterator ) foreach { 
  	        newTree => {
  	          val (leaves, head) = newTree
  	          head <~> leaves
  	        }
  	      }
  	      pile(newLvl, newUnlinked)
  	    }
  	  }
  	  
}
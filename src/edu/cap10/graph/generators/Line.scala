package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object Line {
  val DEF_LENGTH = 5
}

import Line._

case class Line[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {
  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Iterable[V]) :
    (Iterable[V],Int) = (pIter,pIter.size)
      
  	override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Iterable[V], Int))
	(implicit edge:EdgeType = defEdge)
	: Seq[V] =
	  line(data._1 take data._2)
	  
	def line
	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V])
	(implicit edge:EdgeType = defEdge)
	: Seq[V] =
	  iter.head +: { for (pairs <- iter.iterator.sliding(2)) yield {
  	    pairs(1) <~> pairs(0)
  	  } }.toSeq
	  
}
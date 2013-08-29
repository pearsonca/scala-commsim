package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object RectLattice {
  val DEF_W = 5
  val DEF_H = 4
}

import RectLattice._
import math.sqrt

case class RectLattice[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,(Int,Int)] {

  	override def apply
	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], dims:(Int,Int) = (DEF_W,DEF_H))
	(implicit edge:EdgeType = defEdge)
	: Seq[V] =
	  rect(iter,dims) flatten	  
	
  	def rect
  	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], dims:(Int,Int) = (DEF_W,DEF_H))
	(implicit edge:EdgeType = defEdge)
	: Seq[Seq[V]] = {
  	  val (w,h) = dims
  	  val src = iter take (w*h)
  	  val liner = Line(edge)
  	  val lines = src.iterator.grouped(w).map( liner.line ).toSeq
  	  for (linepair <- lines.iterator.sliding(2)) { 
  	    linepair(0).zip(linepair(1)).foreach( topBottom => {
  	      val (top, bottom) = topBottom
  	      top <~> bottom
  	    })
  	  }
  	  lines
  	}
  	
}
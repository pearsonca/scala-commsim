package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object SquareLattice {
  val DEF_SIDE = 5
}

import SquareLattice._
import math.sqrt

case class SquareLattice[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {

  	override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Iterable[V], Int))
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = {
  	  val (iter,side) = data
  	  val rect = RectLattice(edge)
  	  rect(iter,(side,side))
  	} 
  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Iterable[V]) :
  (Iterable[V],Int) =
    (pIter, math.sqrt(pIter.size).toInt)
	  
}
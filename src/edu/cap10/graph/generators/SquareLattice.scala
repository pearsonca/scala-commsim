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
	(iter : Iterable[V], side:Int = DEF_SIDE)
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = {
  	  val rect = RectLattice(edge)
  	  rect(iter,(side,side))
  	} 
	  
}
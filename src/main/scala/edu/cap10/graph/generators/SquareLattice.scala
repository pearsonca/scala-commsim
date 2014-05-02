package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object SquareLattice {
  val DEF_SIDE = 5
}

import SquareLattice._
import math.sqrt

case class SquareLattice[EdgeType](implicit val e:EdgeType) extends Generator[EdgeType,Int] {

  	override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Seq[V], Int))= {
  	  val (iter,side) = data
  	  val rect = RectLattice[EdgeType]
  	  rect(iter,(side,side))
  	} 
  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Seq[V]) =
    (pIter, math.sqrt(pIter.size).toInt)
	  
}
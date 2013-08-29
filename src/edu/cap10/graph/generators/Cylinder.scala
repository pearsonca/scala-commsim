package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object Cylinder {
  val DEF_CIRCUMFERENCE = 10
  val DEF_HEIGHT = 5
}

import Cylinder._

case class Cylinder[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,(Int,Int)] {

    override def apply
	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], circumferenceAndHeight:(Int,Int) = (DEF_CIRCUMFERENCE,DEF_HEIGHT))
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = {
      cylinder(iter,circumferenceAndHeight) flatten
    }

    def cylinder
	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], circumferenceAndHeight:(Int,Int) = (DEF_CIRCUMFERENCE,DEF_HEIGHT))
	(implicit edge:EdgeType = defEdge)
	: Seq[Seq[V]] = {
      val (c, h) = circumferenceAndHeight
      val ringer = Ring(edge)
      val ringed = { for (ring <- iter.take(c*h).grouped(c)) yield { ringer(ring) } }.toSeq
      ringed.iterator.sliding(2) foreach {
        pair => {
          pair(0).iterator.zip(pair(1).iterator).foreach( tb => {
            tb._1 <~> tb._2
          })
        }
      }
      ringed
    }

    
}
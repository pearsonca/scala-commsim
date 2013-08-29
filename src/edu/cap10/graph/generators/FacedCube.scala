package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object FacedCube {
  val DEF_DIM = 2
}

import FacedCube._

case class FacedCube[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {

    override def apply
	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], faceEdges:Int = DEF_DIM)
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = {
      val ringer = Ring(defEdge)
      val (rings, squares) = iter splitAt(faceEdges*4*(faceEdges+1))
      val ringed = { for (ring <- rings.grouped(faceEdges*4)) yield { ringer(ring) } }.toSeq
      ringed.iterator.sliding(2) foreach {
        pair => {
          val left = pair(0).iterator
          val right = pair(1).iterator
          left.zip(right).foreach( pair => {
            pair._1 <~> pair._2
          })
        }
      }
      // now have a cylinder
      Nil
    }
	  

  
}
package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object Cylinder {
  val DEF_CIRCUMFERENCE = 10
  val DEF_HEIGHT = 5
}

import Cylinder._

case class Cylinder[EdgeType](implicit val e:EdgeType) extends Generator[EdgeType,(Int,Int)] {

    override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Seq[V], (Int,Int))) = {
      val (iter, cAndH) = data
      cylinder(iter,cAndH) flatten
    }

    override implicit def default
    [V <: Vertex[EdgeType,V]]
    (pIter: Seq[V]) =
      (pIter, (DEF_CIRCUMFERENCE,DEF_HEIGHT))
    
    val ringer = Ring[EdgeType]
      
    def cylinder
	[V <: Vertex[EdgeType,V]]
	(iter : Seq[V], circumferenceAndHeight:(Int,Int) = (DEF_CIRCUMFERENCE,DEF_HEIGHT))
	: Seq[Seq[V]] = {
      val (c, h) = circumferenceAndHeight
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
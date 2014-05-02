package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object RectLattice {
  val DEF_W = 5
  val DEF_H = 4
}

import RectLattice._
import math.sqrt

case class RectLattice[EdgeType](implicit val e:EdgeType) extends Generator[EdgeType,(Int,Int)] {

  	override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Seq[V], (Int,Int))) = {
  	  val (iter,dims) = data
	  rect(iter,dims) flatten	  
  	}
  	
  	override implicit def default
    [V <: Vertex[EdgeType,V]]
    (pIter: Seq[V]) =
      (pIter, (DEF_W,DEF_H))
  	
  	def rect
  	[V <: Vertex[EdgeType,V]]
	(iter : Seq[V], dims:(Int,Int) = (DEF_W,DEF_H)) = {
  	  val (w,h) = dims
  	  val src = iter take (w*h)
  	  val liner = Line[EdgeType]
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
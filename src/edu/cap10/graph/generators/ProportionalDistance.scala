package edu.cap10.graph.generators

object ProportionalDistance {
  val DEF_BASE = 0.9
  val DEF_DISCOUNT = 0.5
}

import ProportionalDistance._
import edu.cap10.graph.Vertex
import collection.mutable.{Set=>MSet}
import annotation.tailrec
import scala.math.random

case class ProportionalDistance[EdgeType]
(refEdge:EdgeType, defEdge:EdgeType) 
extends Generator[EdgeType,(Double,Double)] {

  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Iterable[V]) =
    (pIter, (DEF_BASE,DEF_DISCOUNT))

  override def apply
  [V <: Vertex[EdgeType,V]]
  (data : (Iterable[V], (Double,Double)) )
  (implicit edge:EdgeType = defEdge) :
  Seq[V] = {
    val (iter, bAndD) = data
    val (p,d) = bAndD
    iter.foldLeft( Set[V]() )( (acc,v) => {
  	  levels( List[Set[V]](v(refEdge).toSet), Set(v) ++ v(refEdge).toSet).reverse.scanLeft(p)( (lp,lvl) => {
  	      v <~> ((lvl &~ acc) filter (_ => random < lp ))
  	      lp*d
  	    })
  	    acc + v
  	  }).toSeq
  	  //iter.toSeq
  }

  	@tailrec final def levels
  	[V <: Vertex[EdgeType,V]]
  	(lvls:Seq[Set[V]], seen: Set[V] = Set.empty[V]) :
  	Seq[Set[V]] =
  	lvls.head match {								// match against the head of the accumulator (lvls)
  	  case empty if empty.isEmpty => lvls.tail		// if head element is empty, we're done
	  case prevLvl => {								// otherwise ...
	      val newLvl = prevLvl.map(					//   for each vertex in the previous level
	          vertex => vertex(refEdge)				//	   get their neighbors via the ref edge flavor
	      ).foldLeft(Set[V]())(						//   fold those neighbors into a single set
	          (total, add) => total ++ add
	      ) -- seen									//   remove the v's we already know the distance to
	      levels(newLvl +: lvls, seen ++ newLvl)	//   recurse with the newly determined level
	  }
    }
  
}
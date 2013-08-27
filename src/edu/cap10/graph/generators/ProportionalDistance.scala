package edu.cap10.graph.generators

object ProportionalDistance {
  val DEF_BASE = 0.5
  val DEF_DISCOUNT = 0.9
}

import ProportionalDistance._
import edu.cap10.graph.Vertex
import collection.mutable.{Set=>MSet}
import annotation.tailrec
import scala.math.random

class ProportionalDistance[EdgeType](refEdge:EdgeType, defEdge:EdgeType) extends Generator[EdgeType,(Double,Double)] {

  	override def apply[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], baseAndDiscount:(Double,Double) = (DEF_BASE,DEF_DISCOUNT))
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = {
  	  val (p,d) = baseAndDiscount
  	  iter.scanLeft( Set[V]() )( (acc,v) => { 
  	    groups( Set(), List(v(refEdge).toSet) ).reverse.scanLeft(p)( (lp,lvl)=> {
  	      v <~> ((lvl &~ acc) filter (_ => random < p))
  	      lp*d 
  	    })
  	    acc+v
  	  })
  	  iter.toSeq
  	}

  	@tailrec
	final def groups[V <: Vertex[EdgeType,V]](acc: Set[V], res:Seq[Set[V]]): Seq[Set[V]] = {
  	  res.head match {
  	    case e if e.isEmpty => res
  	    case step => {
  	      // combine all the one steps, filtering acc
  	      val nextStep = step.map(
  	          // for each step, get all its targeted vertices
  	          v => v(refEdge)
  	      ).foldLeft(Set[V]())(
  	          // merge all those targets into a single set
  	          (l,r) => { l ++ r }
  	      ) &~ acc // then remove the accumulated v's
  	      groups(acc ++ nextStep, nextStep +: res)
  	    }
  	  }
  	}
  
}
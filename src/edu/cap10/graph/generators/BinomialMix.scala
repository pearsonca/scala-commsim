package edu.cap10.graph.generators

object BinomialMix {
  val DEF_RATE = 0.01
}

import edu.cap10.graph.Vertex
import BinomialMix._
import edu.cap10.distributions.DoubleSrc
import edu.cap10.utils._

case class BinomialMix[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Double] {
  override def apply[V <: Vertex[EdgeType,V]](iter : Iterable[V], rate:Double = DEF_RATE)
	(implicit edge:EdgeType = defEdge) : Seq[V] = {
    require(0 <= rate && rate <= 1,"rate parameter is not a probability; provided rate: "+rate.toString)
    if (rate != 0d)
	    iter.dPairs.filter( _ => DoubleSrc.next < rate ).foreach { 
	      p => p._1 !~> p._2
	    }
    iter.toSeq
  }
}
package edu.cap10.graph.generators

object BinomialMix {
  val DEF_RATE = 0.01
}

import edu.cap10.graph.Vertex
import BinomialMix._
import edu.cap10.distributions.DoubleSrc
import edu.cap10.utils._

case class BinomialMix[EdgeType](implicit val e:EdgeType) extends Generator[EdgeType,Double] {
  override def apply
  [V <: Vertex[EdgeType,V]]
  (data : (Seq[V], Double)) = {
    val (iter, rate) = data
    require(0 <= rate && rate <= 1,"rate parameter is not a probability; provided rate: "+rate.toString)
    if (rate != 0d)
	    iter.dPairs.filter( _ => DoubleSrc.next < rate ).foreach { 
	      p => p._1 !~> p._2
	    }
    iter.toSeq
  }
  
  	override implicit def default
	[V <: Vertex[EdgeType,V]]
	(pIter: Seq[V]) = 
	  (pIter,DEF_RATE)
}
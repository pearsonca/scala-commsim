package edu.cap10.graph.generators

import edu.cap10.graph.Vertex
import edu.cap10.graph.Vertex._
import edu.cap10.utils._
import edu.cap10.distributions.RandomDrawSeq

case class Star[EdgeType](implicit val e:EdgeType) extends GeneratorWithVertexData[EdgeType] {

  override def apply
  [V <: Vertex[EdgeType,V]]
  (data : (Seq[V], V)) =
    star(data._1, data._2)
    
  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Seq[V]) =
    (pIter.tail,pIter.head)
  
  def star
  [V <: Vertex[EdgeType,V]]
  (spokes : Seq[V], src: V) = {
    src <~> spokes
    src +: spokes
  }

}
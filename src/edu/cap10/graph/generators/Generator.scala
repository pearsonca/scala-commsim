package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

trait GraphGenerator[EdgeType] {
  def apply
  [V <: Vertex[EdgeType,V]]
  (input: Iterable[V])
  (implicit e:EdgeType) :
  Iterable[V]

}

trait Generator[EdgeType,GeneratorData] extends GraphGenerator[EdgeType] {
  def apply
  [V <: Vertex[EdgeType,V]]
  (input: (Iterable[V], GeneratorData))
  (implicit e:EdgeType) :
  Iterable[V]

  override def apply
  [V <: Vertex[EdgeType,V]]
  (input: Iterable[V])
  (implicit e:EdgeType) :
  Iterable[V] = 
    apply(default(input))
  
  implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Iterable[V]) :
  (Iterable[V],GeneratorData)

  def groupApply[V <: Vertex[EdgeType,V]](pIter: Seq[Seq[V]], data:GeneratorData)
  (implicit e:EdgeType, mapper: (Seq[Seq[V]])=>Seq[V]) : Seq[V] = {
    apply(mapper(pIter), data)
    pIter.flatten
  } 
}
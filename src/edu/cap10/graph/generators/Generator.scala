package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

trait GraphGenerator[EdgeType] {
  
  implicit def e : EdgeType
  
  def apply
  [V <: Vertex[EdgeType,V]]
  (input: Seq[V]) :
  Seq[V]

}

trait GeneratorWithVertexData[EdgeType] extends GraphGenerator[EdgeType] {
  def apply
  [V <: Vertex[EdgeType,V]]
  (input: (Seq[V], V)) :
  Seq[V]
  
  override def apply
  [V <: Vertex[EdgeType,V]]
  (input: Seq[V]) :
  Seq[V] = 
    apply(default(input))
  
  implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Seq[V]) :
  (Seq[V],V)
}

trait Generator[EdgeType,GeneratorData] extends GraphGenerator[EdgeType] {
  def apply
  [V <: Vertex[EdgeType,V]]
  (input: (Seq[V], GeneratorData)) :
  Seq[V]

  override def apply
  [V <: Vertex[EdgeType,V]]
  (input: Seq[V]) :
  Seq[V] = 
    apply(default(input))
  
  implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Seq[V]) :
  (Seq[V],GeneratorData)

  def groupApply[V <: Vertex[EdgeType,V]](pIter: Seq[Seq[V]], data:GeneratorData)
  (implicit mapper: (Seq[Seq[V]])=>Seq[V]) : Seq[V] = {
    apply(mapper(pIter), data)
    pIter.flatten
  } 
}
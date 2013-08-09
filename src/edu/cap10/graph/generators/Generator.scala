package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

trait Generator[EdgeType,GeneratorData] {
  def apply[V <: Vertex[EdgeType,V]](pIter: Iterable[V], data:GeneratorData)(implicit e:EdgeType) : Iterable[V]
}
package edu.cap10.graph

import collection.mutable.SortedSet

object MockEdge extends Enumeration {
  val EDGE = Value
}

import MockEdge.{Value => Edge, _}

case class MockVertex(val id:Long) extends Vertex[Edge,MockVertex] {

  val edges = Map( (EDGE, SortedSet[MockVertex]()) )
  
}

object MockVertexFactory extends Iterator[MockVertex] {
  val underly = Iterator.from(0).map( MockVertex(_) )
  val hasNext = true
  def next = underly.next
}
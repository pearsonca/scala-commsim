package edu.cap10.graph

import collection.mutable.SortedSet

object MockEdge extends Enumeration {
  val MOCKEDGE = Value
}

import MockEdge.{Value => Edge, _}

case class MockVertex(val id:Long) extends Vertex[Edge,MockVertex] {
  override def name = "MockVertex"
  val edges = Map( (MOCKEDGE, SortedSet[MockVertex]()) )
}

object MockVertexFactory extends Iterator[MockVertex] {
  val underly = Iterator.from(0).map( MockVertex(_) )
  val hasNext = true
  def next = underly.next
}
package edu.cap10.graph

import collection.mutable.SortedSet

object MockEdge extends Enumeration {
  val EDGE = Value
}

import MockEdge.{Value => Edge, _}

class MockVertex(val id:Long) extends Vertex[Edge,MockVertex] {

  def edges = Map( (EDGE,SortedSet()) )
  
}

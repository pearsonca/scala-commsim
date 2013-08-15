package edu.cap10.graph.generators

import org.scalatest.FlatSpec
import collection.mutable.Stack

import edu.cap10.graph.MockVertexFactory
import edu.cap10.graph.MockEdge.EDGE
import edu.cap10.graph.MockVertex

import collection.mutable.SortedSet

class RingTest extends FlatSpec {
  
  val refSize = 5
  implicit val e = EDGE
  val generator = Ring(e)
  def src = MockVertexFactory.take(refSize).toSeq
  
  "A Ring" should "return a Seq of its inputs" in {
    val vertices = src
    val res = generator(vertices)
    assert(vertices.size === res.size)
  }
  
  it should "connect each of its elements to their neighboors" in {
    val vertices = src
    val res = generator(vertices)
    for (i <- 0 until vertices.size-1) {
      assert(vertices(i) ?~> vertices(i+1))
    }
    assert(vertices.last ?~> vertices.head)
  }
  
}
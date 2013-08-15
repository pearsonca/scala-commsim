package edu.cap10.graph.generators

import org.scalatest.FlatSpec
import collection.mutable.Stack

import edu.cap10.graph.MockVertexFactory
import edu.cap10.graph.MockEdge.EDGE
import edu.cap10.graph.MockVertex

import collection.mutable.SortedSet

class CliqueTest extends FlatSpec {
  
  val refSize = 5
  implicit val e = EDGE
  val generator = Clique(e)
  def src = MockVertexFactory.take(refSize).toSeq
  
  
  "A Clique" should "return a Seq of its inputs" in {
    val vertices = src
    val res = generator whole vertices
    assert(vertices.size === res.size)
  }
  
  it should "connect all of its elements" in {
    val vertices = src
    val res = generator whole vertices
    res foreach {
      v => {
        assert( v(e).size === (vertices.size - 1) )
        assert( v ?~> vertices.filterNot(v == _) )
      }
    }
  }
  
}
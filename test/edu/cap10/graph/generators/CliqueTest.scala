package edu.cap10.graph.generators

import org.scalatest.FlatSpec
import collection.mutable.Stack

import edu.cap10.graph.MockVertexFactory
import edu.cap10.graph.MockEdge.MOCKEDGE
import edu.cap10.graph.MockVertex

import collection.mutable.SortedSet

class CliqueTest extends FlatSpec {
  
  val refSize = 5
  implicit val e = MOCKEDGE
  val generator = Clique(e)
  def src = MockVertexFactory.take(refSize).toSeq
  
  
  "A Clique" should "return a Seq of its inputs" in {
    val vertices = src
    val res = generator clique vertices
    assert(vertices.size === res.size)
  }
  
  it should "connect all of its elements" in {
    val vertices = src
    val res = generator clique vertices
    res foreach {
      v => {
        assert( v(e).size === (vertices.size - 1) )
        assert( v ?~> vertices.filterNot(v == _) )
      }
    }
  }
  
}
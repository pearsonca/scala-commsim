package edu.cap10.graph.generators

import org.scalatest.FlatSpec
import collection.mutable.Stack

import edu.cap10.graph.MockVertexFactory
import edu.cap10.graph.MockEdge.MOCKEDGE
import edu.cap10.graph.MockVertex

import collection.mutable.SortedSet

import math.pow

class TreeTest extends FlatSpec {
  
  val refWidth = 3
  val refDepth = 3
  val refSize = (1 - pow(refWidth,(refDepth+1)).toInt)/(1-refWidth)
  println(refSize)
  
  implicit val e = MOCKEDGE
  val generator = Tree(e)
  def src = MockVertexFactory.take(refSize).toSeq
  
  
  "A Tree" should "return a Seq of its inputs" in {
    val vertices = src
    val res = generator( vertices, refWidth )
    assert(vertices.size === res.size)
    for (v <- res) println((v,v(e).size))
  }
  
//  it should "connect all of its elements" in {
//    val vertices = src
//    val res = generator(vertices)
//    res foreach {
//      v => {
//        assert( v(e).size === (vertices.size - 1) )
//        assert( v ?~> vertices.filterNot(v == _) )
//      }
//    }
//  }
  
}
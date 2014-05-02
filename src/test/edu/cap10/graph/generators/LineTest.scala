package edu.cap10.graph.generators
//
//import org.scalatest.FlatSpec
//import collection.mutable.Stack
//
//import edu.cap10.graph.MockVertexFactory
//import edu.cap10.graph.MockEdge
//import edu.cap10.graph.MockEdge._
//import edu.cap10.graph.MockVertex
//
//import collection.mutable.SortedSet
//
//import math.pow
//
//class LineTest extends FlatSpec {
//  
//  val refSize = 5
//  
//  implicit val e = MOCKEDGE
//  val generator = Line[MockEdge.Value]
//  def src = MockVertexFactory.take(refSize).toSeq
//  
//  
//  "A Line" should "return a Seq of its inputs" in {
//    val vertices = src
//    val res = generator( vertices )
//    assert(vertices.size === res.size)
//    for (v <- res) println(v)
//  }
//  
////  it should "connect all of its elements" in {
////    val vertices = src
////    val res = generator(vertices)
////    res foreach {
////      v => {
////        assert( v(e).size === (vertices.size - 1) )
////        assert( v ?~> vertices.filterNot(v == _) )
////      }
////    }
////  }
//  
//}
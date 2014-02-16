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
//class RectTest extends FlatSpec {
//  
//  val (refWidth,refHeight) = (5,4)
//  
//  implicit val e = MOCKEDGE
//  val generator = RectLattice[MockEdge.Value]
//  def src = MockVertexFactory.take(refWidth*refHeight).toSeq
//  
//  
//  "A RectLattice" should "return a Seq of its inputs" in {
//    val vertices = src
//    val res = generator( vertices, (refWidth,refHeight) )
//    assert(vertices.size === res.size)
//  }
//  
//  it should "also provide a matrix version of its output" in {
//    val vertices = src
//    val res = generator rect( vertices, (refWidth,refHeight) )
//    assert(res.size === refHeight)
//    res foreach {
//      line => {
//        assert( line.size === refWidth )
//      }
//    }
//  }
//  
//}
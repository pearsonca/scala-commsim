package edu.cap10.graph.generators

import edu.cap10.graph.Vertex
import edu.cap10.distributions.RandomDrawSeq
import edu.cap10.utils._

import scala.annotation.tailrec

import scala.collection.Seq.{fill => repeat}

object CliqueHierarchy {
  val DEF_SIZE = 3
}

import CliqueHierarchy._

case class CliqueHierarchy[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {
  // TODO allow for flexible clique sizes by changing generator input

  override def apply
  [V <: Vertex[EdgeType,V]]
  (iter : Iterable[V], size:Int = DEF_SIZE)
  (implicit edge:EdgeType = defEdge) :
  Seq[V] =
    grouped(Clique(edge).all(iter, size),size)
  
  @tailrec final def grouped
  [V <: Vertex[EdgeType,V]]
  (src: Seq[Seq[V]], size:Int = DEF_SIZE)
  (implicit edge:EdgeType = defEdge) :
  Seq[V] = {
    src.size match {
      case 1 => src(0)
      case s if s <= size => up(src.iterator, s)
      case s =>
        val iter = src.iterator
        val div = s / size
        s % size match {
	      case 0 =>
	        grouped( cliqueGroupsEach(src,size) , size)
	      case rem if rem > div =>
	        val (single, large) = src.splitAt(rem)
	        grouped( cliqueGroups(single) +: cliqueGroupsEach(large, size) )
	      case rem =>
	        //val (small, large) 
	        grouped( repeat(rem)( up(iter, size+1) ) ++ repeat(div - rem)( up(iter, size) ), size)
	    }
    }
  }
  
  def cliqueGroups
  	[V <: Vertex[EdgeType,V]]
  	(src: Iterable[Seq[V]], linkCount:Int = 1)
  	(implicit edge:EdgeType = defEdge)
  	: Seq[V] = {
    for ( (leftGroup, rightGroup) <- src.uPairs )
      // TODO take advantage of linkCount to allow for multiple edges between higher level components
      leftGroup.random <~> rightGroup.random
    src.toSeq flatten
  }
  
  private def cliqueGroupsEach
  	[V <: Vertex[EdgeType,V]]
  	(src: Iterable[Seq[V]], size: Int, linkCount: Int = 1)
  	(implicit edge:EdgeType = defEdge)
  	: Seq[Seq[V]] = { 
	  for (subset <- src.grouped(size)) yield cliqueGroups(subset)
  }.toSeq
  
  def up[V <: Vertex[EdgeType,V]](src: Iterator[Seq[V]], size:Int = DEF_SIZE)
  (implicit edge:EdgeType = defEdge) : Seq[V] = {
    val res = src.take(size).toSeq
//    UniqueDirectedPairs(0 until res.length) foreach { (lr) => res(lr._1).random ~> res(lr._2).random }
    for (	srci <- 0 until res.size; 
    		tarj <- (0 until res.size).filter(_ != srci);
    		srcp = res(srci).random;
    		tarp = res(tarj).random if tarp != srcp
    	)
      srcp ~> tarp;
    res flatten
  }
}
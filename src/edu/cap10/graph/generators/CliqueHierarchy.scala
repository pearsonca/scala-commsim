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

case class CliqueHierarchy[EdgeType](implicit val e:EdgeType) extends Generator[EdgeType,Int] {
  // TODO allow for flexible clique sizes by changing generator input

  val cliquer = Clique[EdgeType]
  
  override def apply
  [V <: Vertex[EdgeType,V]]
  (data : (Seq[V], Int)) =
    grouped(cliquer.all(data),data._2)
  
  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Seq[V]) =
    (pIter,DEF_SIZE)
    
  implicit def seqSeqToSeqV
  [V <: Vertex[EdgeType,V]]
  (src:Seq[Seq[V]]) :
  Seq[V] =
    src.view.map { _.random }

    
  @tailrec final def grouped
  [V <: Vertex[EdgeType,V]]
  (src: Seq[Seq[V]], size:Int = DEF_SIZE) :
  Seq[V] = {
    src.size match {
      case 1 => src(0)
      case s if s <= size => up(src.iterator, s)
      case s =>
        val iter = src.iterator
        val div = s / size
        s % size match {
	      case 0 =>
	        val newGroups = for (group <- src.grouped(size).toSeq) yield { cliquer.groupApply(group, size) }
	        grouped( newGroups , size)
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
  	(src: Seq[Seq[V]], linkCount:Int = 1)
  	: Seq[V] = {
    for ( (leftGroup, rightGroup) <- src.uPairs )
      // TODO take advantage of linkCount to allow for multiple edges between higher level components
      leftGroup.random <~> rightGroup.random
    src.toSeq flatten
  }
  
  private def cliqueGroupsEach
  	[V <: Vertex[EdgeType,V]]
  	(src: Seq[Seq[V]], size: Int, linkCount: Int = 1)
  	: Seq[Seq[V]] =
  	  for (group <- src.grouped(size).toSeq) yield { 
  	    cliquer.groupApply(group, size)
  	  }
  
  def up[V <: Vertex[EdgeType,V]](src: Iterator[Seq[V]], size:Int = DEF_SIZE) : Seq[V] = {
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
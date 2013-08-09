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

  override def apply[V <: Vertex[EdgeType,V]](iter : Iterable[V], size:Int = DEF_SIZE)
  (implicit edge:EdgeType = defEdge) : Seq[V] = { 
    grouped(Clique(edge).all(iter, size),size)
  }
  
  @tailrec final def grouped[V <: Vertex[EdgeType,V]](src: Seq[Seq[V]], size:Int = DEF_SIZE)
  (implicit edge:EdgeType = defEdge) : Seq[V] = {
    if (src.size == 1) // done condition for exactly divisible pieces
      src(0)
    else if (src.size <= size) { // done condition when there are too few (or exactly the right number of) items to clique
      up(src.iterator, src.size)
    } else {
      val iter = src.iterator
      val div = src.size / size
      src.size % size match {
	      case 0 => grouped(repeat(div)( up(iter, size) ), size )
	      case rem if rem > div =>  
	        grouped( up(iter, rem) +: repeat(div)( up(iter, size) ), size)
	      case rem =>     
	        grouped( repeat(rem)( up(iter, size+1) ) ++ repeat(div - rem)( up(iter, size) ), size)
	  }
    }
  }
  
  def up[V <: Vertex[EdgeType,V]](src: Iterator[Seq[V]], size:Int = DEF_SIZE)
  (implicit edge:EdgeType = defEdge) : Seq[V] = {
    val res = src.take(size).toSeq
//    UniqueDirectedPairs(0 until res.length) foreach { (lr) => res(lr._1).random ~> res(lr._2).random }
    for (	srci <- 0 until res.size; 
    		tarj <- (0 until res.size) if tarj != srci;
    		srcp = res(srci).random;
    		tarp = res(tarj).random if tarp != srcp
    	)
      srcp ~> tarp;
    res flatten
  }
}
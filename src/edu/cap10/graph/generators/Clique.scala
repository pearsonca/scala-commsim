package edu.cap10.graph.generators

import edu.cap10.graph.Vertex
import edu.cap10.graph.Vertex._
import edu.cap10.utils._

object Clique {
  val DEF_SIZE = 3
}

import Clique._

/** Clique is a [[edu.cap10.graph.generators.Generator]] that completely connects 
 * [[edu.cap10.graph.Vertex]] instances with default `defEdge` of `EdgeType`.
 * 
 * @constructor must be provided an item to provide the default `edge` type
 * when constructing edges.
 */
case class Clique[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {
  
    //def <~>[V <: Vertex[EdgeType,V]](implicit edge : EdgeType = defEdge) : ((V,V)) => Unit = Vertex.<~>[EdgeType,V].tupled
  
	override def apply[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], size:Int = DEF_SIZE)
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = 
	  whole(iter.take(size))
	
	def whole[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V])
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = {
	  iter.toSeq.flow( _.uPairs.foreach {
	    pair =>
	      pair._1 <~> pair._2 
	  } )
	}
	
	def add[V <: Vertex[EdgeType,V]](orig : Seq[V], add: V)
	(implicit e:EdgeType) = add <~> orig

	def all[V <: Vertex[EdgeType,V]](iter : Iterable[V], size : Int = DEF_SIZE)
	(implicit edge:EdgeType = defEdge) : Seq[Seq[V]] = {
	  require(size > 0,"Clique size must be > 0.")
	  val iterSize = iter.size
	  require(iterSize > 0,"Attempted to clique an empty collection.")
	  
	  if (iterSize == 1)
	    Seq(Seq(iter.head)) // if there's only one, done
	  else if (iterSize <= size)
        Seq(apply(iter,iterSize))
      else iterSize % size match {
      	case 0 => each(iter, size) // exact division
      	case rem => iterSize / size match {
      	  case div if rem == div => each(iter, size+1)  
      	  case div if rem < div => 
      	    val (left,right) = iter.splitAt(div*size)
      	    each(left, size) :+ whole(right)
      	  case div => 
      	    val (left,right) = iter.splitAt(div*(size+1))
      	    each(left, size+1) :+ whole(right)
      	}
      }
    }
	
	private def each[V <: Vertex[EdgeType,V]](iter : Iterable[V], size : Int)
	(implicit edge:EdgeType = defEdge) : Seq[Seq[V]] = {
	  for (group <- iter.grouped(size)) yield whole(group) 
	}.toSeq
}
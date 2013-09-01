package edu.cap10.graph.generators

import edu.cap10.graph.Vertex
import edu.cap10.graph.Vertex._
import edu.cap10.utils._
import edu.cap10.distributions.RandomDrawSeq

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
    
	override def apply
	[V <: Vertex[EdgeType,V]]
	(data : (Iterable[V], Int))
	(implicit edge:EdgeType = defEdge)
	: Seq[V] = 
	  clique(data._1 take data._2)
	
	override implicit def default
	[V <: Vertex[EdgeType,V]]
	(pIter: Iterable[V]) :
      (Iterable[V],Int) = (pIter,pIter.size)

	  
	def clique
	  [V <: Vertex[EdgeType,V]]
	  (iter : Iterable[V])
	  (implicit edge:EdgeType = defEdge)
	  : Seq[V] = {
	    val vertices = iter.toSeq					// collect the vertices that will be returned
	    for ( (left, right) <- vertices.uPairs )	// for each unique, unordered pair in that col.
	      left <~> right							//   form a bidirectional edge between those
	    vertices									// return the vertices
	}
	
	def add
	[V <: Vertex[EdgeType,V]]
	(orig : Seq[V], add: V)
	(implicit e:EdgeType)
	: V =
	  add <~> orig

	def all
	[V <: Vertex[EdgeType,V]]
	(data : (Iterable[V], Int))
	(implicit edge:EdgeType = defEdge)
	: Seq[Seq[V]] = {
	    val (iter, size) = data
	  require(size > 0,"Clique size must be > 0.")
	  iter.size match {
	    case 0 => throw new IllegalArgumentException("Attempted to clique an empty collection.")
	    case 1 => Seq(Seq(iter.head)) // the single item edge case; TODO: error / warn?
	    case iSize => iSize % size match {
	      case 0 => each(iter, size)
	      case rem => iSize / size match {
	        case div if rem == div => each(iter, size+1)  
      	    case div => 
      	      val refSize = if (rem < div) size else size+1
      	      val (left, right) = iter.splitAt(div*refSize)
      	      each(left, refSize) :+ clique(right)
	      }
	    }
	  }
    }
	
	private def each
	[V <: Vertex[EdgeType,V]]
	(iter : Iterable[V], size : Int)
	(implicit edge:EdgeType = defEdge)
	: Seq[Seq[V]] = {
	  for (group <- iter.grouped(size)) yield clique(group) 
	}.toSeq
}
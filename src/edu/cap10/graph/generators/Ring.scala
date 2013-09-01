package edu.cap10.graph.generators

import edu.cap10.graph.Vertex

object Ring {
  val DEF_WIDTH = 1
}

import Ring._

case class Ring[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {

  override def apply
  [V <: Vertex[EdgeType,V]]
  (data : (Iterable[V], Int))
  (implicit edge:EdgeType = defEdge) :
  Seq[V] = {
    val (iter, width) = data
    val res = iter.toSeq
    res.size match {
      case size if (size+1)/2 <= width => 
        throw new IllegalArgumentException("width less than or equal to number of vertices; width: "+width+", size: "+size)
//      case size if size == width+1 => Ring(edge)(iter) // there is a Clique case, but it complicated - basically, if size is odd & (size+1)/2 - 1 == width
      case size => 
        val (it1,it2) = iter.iterator.duplicate
        for (grp <- it1.sliding(width+1, 1).withPadding(it2.next)) {
          grp.head ~> grp.tail
        }
        res
    }
  }
  
  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Iterable[V]) :
  (Iterable[V],Int) =
    (pIter, DEF_WIDTH)

  
}

case class DirectedRing[EdgeType](defEdge:EdgeType) extends Generator[EdgeType,Int] {

  override implicit def default
  [V <: Vertex[EdgeType,V]]
  (pIter: Iterable[V]) :
  (Iterable[V],Int) =
    (pIter, DEF_WIDTH)
  
  override def apply
  [V <: Vertex[EdgeType,V]]
  (data : (Iterable[V], Int))
  (implicit edge:EdgeType = defEdge) :
  Seq[V] = {
    val (iter, width) = data
    val res = iter.toSeq
    res.size match { // TODO actual logic is width >= size/2
      case size if size <= width => throw new IllegalArgumentException("width greater than or equal to size; width: "+width+", size: "+size)
      case size if size == width+1 => Clique(edge)(iter)
      case size => 
        val (it1,it2) = iter.iterator.duplicate
        for (grp <- it1.sliding(width+1, 1).withPadding(it2.next)) {
          grp.head ~> grp.tail
        }
        res
    }
  }
  
}
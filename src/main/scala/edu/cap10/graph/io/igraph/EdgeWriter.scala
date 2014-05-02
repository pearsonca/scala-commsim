package edu.cap10.graph.io.igraph

import edu.cap10.graph.Vertex
import java.io._

trait GenericEdgeWriter[EdgeType, V <: Vertex[EdgeType,V]] {
  def apply(vertices:Iterable[V])(implicit writer:PrintWriter) = {
    for ( (v1,v2,e) <- transform(vertices) ) writer.println(v1.id + " " + v2.id + " " + e)
	writer.flush
	writer
  }
  
  def transform(vertices:Iterable[V]) : Iterable[(V,V,EdgeType)]
}
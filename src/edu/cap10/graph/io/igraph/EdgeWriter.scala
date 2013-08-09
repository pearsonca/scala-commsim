package edu.cap10.graph.io.igraph

import edu.cap10.graph.Vertex
import java.io._

object EdgeWriter {
	def apply[EdgeType, V <: Vertex[EdgeType,V]](vertices:Iterable[V])(implicit writer:PrintWriter) = {
	  for (src <- vertices; edgeType <- src.edgeTypes; tar <- src(edgeType))
	    writer.println(src.id + " " + tar.id + " " + edgeType)
	  writer.flush
	  writer
	}
}
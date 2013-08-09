package edu.cap10.graph.io.igraph

import edu.cap10.graph.Vertex
import java.io._

object VertexWriter {
  def apply[EdgeType, V <: Vertex[EdgeType,V]](vertices:Iterable[V])(implicit writer:PrintWriter) = {
    for (vertex <- vertices) writer.println(vertex)
    writer.flush
    writer
  }
}
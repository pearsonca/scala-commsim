package edu.cap10.person

//import edu.cap10.graph.io.igraph.GenericEdgeWriter
//
//object PersonEdgeWriter extends GenericEdgeWriter[Community.Value, PersonLike] {
//	
//	def transform(vertices:Iterable[PersonLike]) = {
//	  val thing = for (	
//	      src <- vertices; 
//		  (e,tars) <- src.edges;
//		  tar <- tars) yield link(src, e, tar)
//	  thing flatten
//	}
//	
//	private def link(p1:PersonLike, e:Community.Value, p2:PersonLike) :
//	Iterable[(PersonLike,PersonLike,Community.Value)] =
//	  Iterable((p1,p2,e))
//	private def link(p1:PersonLike,e:Community.Value, p2:PersonLike, p3:PersonLike, others:PersonLike*) : 
//	Iterable[(PersonLike,PersonLike,Community.Value)] = {
//	  Iterable((p1,p2,e),(p1,p3,e)) ++ link(p1,e,others)
//	}
//	private def link(p1:PersonLike, e:Community.Value, others:Iterable[PersonLike]) :
//	Iterable[(PersonLike,PersonLike,Community.Value)] = {
//	  for (p2 <- others) yield { (p1,p2,e) }
//	}
//}

package edu.cap10.graph

//import scala.collection.mutable.Buffer
import scala.collection.mutable.SortedSet
import scala.collection.TraversableLike
import scala.collection.immutable.Stream.continually;

object Vertex {
  def <~>[EdgeType, V <: Vertex[EdgeType, V]]
  (implicit edge:EdgeType) : (V,V) => Unit = (l,r) => l <~> r
  
  def ~>[EdgeType,V <: Vertex[EdgeType,V]](implicit edge:EdgeType) : (V,V) => Unit = (l,r) => l ~> r
  
  implicit def vTupler[V <: Vertex[_,V],T](f:(V,V)=>T) = f.tupled
  
  implicit def subclassToRepr[U <: Vertex[_,U], V <: U](v:V) = v.self
  implicit def travToRepr[
    U <: Vertex[_,U], V <: U,
    Trav <: TraversableLike[U,Trav]]
  (trav:TraversableLike[U,Trav]) = trav map { v => subclassToRepr(v) }
}

trait Vertex[EdgeType, Repr <: Vertex[EdgeType, Repr]] extends Ordering[Repr] {
  type Self = Repr
  def compare(x:Repr, y:Repr) = ord.compare(x, y)
  implicit val ord = Ordering.by( (v:Repr) => v.id )
  def id : Long
  def self = this.asInstanceOf[Repr]
  
//  def <~>(implicit edge : EdgeType) : ((Repr,Repr)) => Unit = Vertex.<~>[EdgeType,Repr].tupled
  
  def apply(e: EdgeType) = edges(e)
  def edgeTypes : Iterable[EdgeType] = edges.keys
  def edges : Map[EdgeType, SortedSet[Repr]]
  
  protected def edgeCollSrc = continually(SortedSet[Repr]())
  
  def ~>[R <: Repr](other:Repr*)
  (implicit e: EdgeType) = {
    this(e) ++= other; self
  }
  def <~(other:Repr*)
  (implicit e:EdgeType)  = {
    other foreach { _ ~> self }; self
  }

  def ~/>[R <: Repr](other:Repr*)
  (implicit e: EdgeType)  = {
    this(e) --= other; self
  }
  def </~(other:Repr*)
  (implicit e:EdgeType)  = {
    other foreach { _ ~/> self }; self
  }
  
  def ?~>(other:Repr)
  (implicit e: EdgeType) = {
    this(e) contains other
  }
  def <~?(other:Repr)
  (implicit e: EdgeType) = {
    other ?~> self
  }

  def !~>(other:Repr*)
  (implicit e: EdgeType)  = {
    other.foreach( o => if( this ?~> o ) {
      this ~/> o
    } else {
      this ~> o
    } )
    self
  }
  def <~!(other:Repr*)
  (implicit e: EdgeType)  = {
    other.foreach( o => if( o ?~> self ) {
      o ~/> self
    } else {
      o ~> self
    } )
    self
  }
  
  def <~>(other:Repr*)(implicit e: EdgeType) = ~> (other:_*) <~ (other:_*)
  def </~/>(other:Repr*)(implicit e: EdgeType) = ~/> (other:_*) </~ (other:_*)
  
  def <~!~>(other:Repr*)(implicit e: EdgeType) = !~> (other:_*) <~! (other:_*)

}
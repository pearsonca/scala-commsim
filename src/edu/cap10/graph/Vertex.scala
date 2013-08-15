package edu.cap10.graph

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

trait Vertex[EdgeType, Repr <: Vertex[EdgeType, Repr]] extends Ordered[Repr] with Function1[EdgeType,SortedSet[Repr]] {
  
  def compare(that:Repr) = id.compare(that.id)
  
  def id : Long
  def self = this.asInstanceOf[Repr]
  
  override val hashCode = id.hashCode
  override def toString = id.toString
  override def equals(other:Any) = other match {
    case o:Vertex[EdgeType,Repr] => o.id == id
    case _ => false
  }
//  def <~>(implicit edge : EdgeType) : ((Repr,Repr)) => Unit = Vertex.<~>[EdgeType,Repr].tupled
  
  def apply(e: EdgeType) = edges(e)
  def edgeTypes : Iterable[EdgeType] = edges.keys
  def edges : Map[EdgeType, SortedSet[Repr]]
  
  protected def edgeCollSrc = continually(SortedSet[Repr]())
  
  def ~>[R <: Repr](other:Repr)
  (implicit e: EdgeType) : Repr = {
    this(e) += other
    self
  }
  def ~>[R <: Repr](other1:Repr, other2:Repr, others:Repr*)
  (implicit e: EdgeType) : Repr = {
    this(e)+=(other1,other2,others:_*)
    self
  }
  def ~>[R <: Repr](others:Iterable[Repr])
  (implicit e: EdgeType) : Repr = {
    this(e) ++= others; self
  }
  
  
  def <~(other:Repr)
  (implicit e:EdgeType) : Repr = {
    other ~> self; self
  }
  def <~(other1:Repr, other2:Repr, others:Repr*)
  (implicit e:EdgeType) : Repr = {
    other1 ~> self; other2 ~> self
    <~(others)
  }
  def <~(others:Iterable[Repr])
  (implicit e:EdgeType) : Repr = {
    others foreach { _ ~> self }; self
  }

  def ~/>[R <: Repr](other:Repr*)
  (implicit e: EdgeType)  = {
    this(e) --= other; self
  }
  def </~(other:Repr*)
  (implicit e:EdgeType)  = {
    other foreach { _ ~/> self }; self
  }
  
  //n.b.: this(e) yields a Set, and Sets are a Element => Boolean, specifically the ``contains'' function
  def ?~>(other:Repr)
  (implicit e: EdgeType) : Boolean =
    this(e)(other)
  def ?~>(other1:Repr, other2:Repr, others:Repr*)
  (implicit e: EdgeType) : Boolean =
    (self ?~> other1) && (self ?~> other2) && (self ?~> others)
  def ?~>(others:Iterable[Repr])
  (implicit e: EdgeType) : Boolean =
    others forall { this(e) }
  
  def <~?(other:Repr)
  (implicit e: EdgeType) = other ?~> self
  // TODO other versions

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
  
  def <~>(other:Repr)(implicit e: EdgeType) : Repr = ~>(other)<~(other)
  def <~>(other1:Repr, other2:Repr, others:Repr*)(implicit e: EdgeType) : Repr = <~>(other1)<~>(other2)<~>(others)
  def <~>(others:Iterable[Repr])(implicit e: EdgeType) = ~>(others)<~(others)
  
  def </~/>(other:Repr*)(implicit e: EdgeType) = ~/> (other:_*) </~ (other:_*)
  
  def <~!~>(other:Repr*)(implicit e: EdgeType) = !~> (other:_*) <~! (other:_*)

}
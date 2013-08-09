package edu.cap10.person

case class PlotterFactory(startId : Long = 0) {
  private def loop(i:Long) : Stream[PersonLike] =
    Plotter(i) #:: loop(i+1)
  val src : Stream[PersonLike] = loop(startId)
}

import Community.{Value => CValue}
import scala.collection.mutable.SortedSet;

case class Plotter(val id : Long) extends PersonLike {
  override val toString = super.toString + " plotter"
  override val edges = Map[CValue,SortedSet[PersonLike]]()	
  override def messages(commType:CValue) = Nil // actually do nothing but record received messages
}
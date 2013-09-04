package edu.cap10.person

case class PlotterFactory(startId : Long = 0, sub : LoggerSubstrate) {
  private def loop(i:Long) : Stream[PersonLike] =
    Plotter(i, sub) #:: loop(i+1)
  val src : Stream[PersonLike] = loop(startId)
}

import Community.{Value => CValue}
import scala.collection.mutable.SortedSet;

case class Plotter(val id : Long, val substrate : LoggerSubstrate) extends PersonLike {
  override val name = "Plotter"
  override val edges = Map[CValue,SortedSet[PersonLike]]()	
  override def messages(commType:CValue) = Nil // actually do nothing but record received messages
}
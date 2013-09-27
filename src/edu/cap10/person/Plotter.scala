package edu.cap10.person

import edu.cap10.sim.Logger

case class PlotterFactory(startId : Long = 0, logger : Logger[(Community.Value, Vocabulary.Value, PersonLike),PersonLike]) {
  private def loop(i:Long) : Stream[PersonLike] =
    Plotter(i, logger) #:: loop(i+1)
  val src : Stream[PersonLike] = loop(startId)
}

import Community.{Value => CValue}
import scala.collection.mutable.SortedSet;

case class Plotter(val id : Long, override val logger : Logger[(Community.Value, Vocabulary.Value, PersonLike),PersonLike]) extends PersonLike {
  override val name = "Plotter"
  override val edges = Map[CValue,SortedSet[PersonLike]]()	
  override def messages(commType:CValue) = Nil // actually do nothing but record received messages
}
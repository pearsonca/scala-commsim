package edu.cap10.graph.generators

import edu.cap10.graph._

import edu.cap10.person.Person
import edu.cap10.channels.Path
import edu.cap10.message._
import edu.cap10.sim._

import scala.collection.mutable._
import scala.collection.mutable.Set
import scala.collection.mutable.MultiMap
import scala.collection.Iterable
import scala.actors.Actor

abstract class Generator[SrcType] {
  def generate(s:SrcType) : PersonGraph
  def apply(src:SrcType) = generate(src)
}

object Sequential extends Generator[Int] {
  override def generate(count:Int) = PersonGraph(personSeq(count))
  def personSeq(count:Int) : Iterable[Person] = personSeqFrom(1)(count)
  def personSeqFrom(start:Int)(count:Int) = for (id <- start to (count+start-1)) yield Person(id)
}

object Clique extends Generator[Int] {
  override def generate(count:Int) = cliquer(count)

  def cliquer(count:Int) = {
    val pg = Sequential(count)
    pg fluenteach((p1:Person)=>{
      for (p2 <- pg filter(_ != p1)) p1 + Path(p2)
    })
  }
    
}

object TwoGroupEachClique extends Generator[(Int,Int)] {
  override def generate(sizes:(Int,Int)) = PersonGraph(
    Clique.cliquer(Sequential.tuplerFrom(1)(sizes._1)) ++ Clique.cliquer(Sequential.tuplerFrom(sizes._1+1)(sizes._2))
  )
}
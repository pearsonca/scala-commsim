package edu.cap10.graph.generator

import edu.cap10.person.Person
import edu.cap10.channels.Path
import scala.collection.mutable._
import scala.collection.mutable.Set
import scala.collection.mutable.MultiMap

class PersonGraph(val people:Iterable[Person]) {
//  def +(that:PersonGraph) = new PersonGraph(this.people++that.people, this.channels ++ that.channels)
  override def toString = {
    "People: [ "+people+" ]\nChannels :\n"+(for (p<-people) yield { "->" + p.channels mkString ", " +"\n" } )
  }
}

object PersonGraph {
  def apply(people:Iterable[Person], channels:MultiMap[Person,Path]) = new PersonGraph(people,channels)
  implicit def tuple2PersonGraph = (tuple:(Iterable[Person], MultiMap[Person,Path])) => new PersonGraph(tuple._1,tuple._2)
}

abstract class Generator[SrcType] {
  def generate : (SrcType) => PersonGraph
  def apply(src:SrcType) = generate(src)
}

object Sequential extends Generator[Int] {
  override def generate = (count:Int) => tupler(count)
  def tupler = tuplerFrom(1) _
  def tuplerFrom(start:Int)(count:Int) = {
    val people = for (id <- start to (count+start-1)) yield Person(id)
    var channels = new Map[Person,Set[Path]] with MultiMap[Person,Path]
    // val channels = Iterable[Channel]()
    (people,channels)
  }
}

object Clique extends Generator[Int] {
  override def generate = (count:Int) => {
    val (people, _) = Sequential.tupler(count) 
    (people,cliquer(people))
  }
  def cliquer(people:Iterable[Person]) : MultiMap[Person,Path] = {
    var channels = new Map[Person,Set[Path]] with MultiMap[Person,Path]
    for (p1 <- people) {
      channels += (p1->Set[Channel](people.filter(_!=p1)))
      //(people.filter(_!=p1))
    }
    channels
  }
    
}

object TwoGroupEachClique extends Generator[(Int,Int)] {
  override def generate = (sizes:(Int,Int)) => {
    val (group1, _) = Sequential.tuplerFrom(1)(sizes._1)
    val (group2, _) = Sequential.tuplerFrom(sizes._1+1)(sizes._2)
    (group1++group2, Clique.cliquer(group1)++Clique.cliquer(group2))
  }
}
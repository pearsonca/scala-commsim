package edu.cap10.graph.generator

import edu.cap10.person.Person
import edu.cap10.channels.Path
import edu.cap10.message._
import scala.collection.mutable._
import scala.collection.mutable.Set
import scala.collection.mutable.MultiMap

class PersonGraph(val people : Set[Person]) {
//  def +(that:PersonGraph) = new PersonGraph(this.people++that.people, this.channels ++ that.channels)
  override def toString = {
    "People: [ "+people+" ]\nChannels :\n"+(for (p<-people) yield { "->" + p.channels mkString ", " +"\n" } )
  }
}

object PersonGraph {
  def apply(people:Set[Person]) = new PersonGraph(people)
  implicit def people2PersonGraph = (p:Set[Person]) => new PersonGraph(p)
}

abstract class Generator[SrcType] {
  def generate : (SrcType) => PersonGraph
  def apply(src:SrcType) = generate(src)
}

object Sequential extends Generator[Int] {
  override def generate = (count:Int) => new PersonGraph(tupler(count))
  def tupler(count:Int) : Set[Person] = tuplerFrom(1)(count)
  def tuplerFrom(start:Int)(count:Int) = {
    val res = Set[Person]()
    for (id <- start to (count+start-1)) res+=Person(id)
    res
  }
}

object Clique extends Generator[Int] {
  override def generate = (count:Int) => {
    cliquer(Sequential.tupler(count))
  }
  def cliquer(people:Set[Person]) : Set[Person] = {
    for (p1 <- people; p2 <- people if p1 != p2) {
      p1 + Path(p2,DefaultLogger(p1,p2))
    }
    people
  }
    
}

object TwoGroupEachClique extends Generator[(Int,Int)] {
  override def generate = (sizes:(Int,Int)) => {
    val group1 = Clique.cliquer(Sequential.tuplerFrom(1)(sizes._1))
    val group2 = Clique.cliquer(Sequential.tuplerFrom(sizes._1+1)(sizes._2))  
    group1++group2
  }
}
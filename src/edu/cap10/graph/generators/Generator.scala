package edu.cap10.graph.generators

import edu.cap10.person.Person
import edu.cap10.channels.Path
import edu.cap10.message._
import edu.cap10.sim._

import scala.collection.mutable._
import scala.collection.mutable.Set
import scala.collection.mutable.MultiMap
import scala.collection.Iterable
import scala.actors.Actor

class PersonGraph(val people : Iterable[Person]) extends Actor {
  def +(that:PersonGraph) = new PersonGraph(this.people++that.people)
  override def toString = {
    val chan = (for (p<-people) yield { p+" :"+(p.channels mkString ",") }) mkString ",\n"
    "People: [ "+people+" ]\nChannels :\n"+chan
  }
  def setLogging(logFactory:LoggerFactory) = {
    people foreach {
      _.channels foreach ((p:Path) => {
        p setLogger( logFactory.create(p) )
      })
    }
    this
  }
  override def act = loop {
    react {
      case SimTask("START",c) => {
        people foreach { _.start ! SimTask("START",c) }
      }
      case SimTask("STOP",c) => {
        people foreach { _ ! SimTask("STOP",c) }
        exit
      }
      case task : SimTask => people foreach { _ ! task }
      case msg => println("Unhandled "+msg)
    }
  }
}

object PersonGraph {
  def apply(people:Iterable[Person]) = new PersonGraph(people)
  implicit def people2PersonGraph(p:Set[Person]) = new PersonGraph(p)
}

abstract class Generator[SrcType] {
  def generate(s:SrcType) : PersonGraph
  def apply(src:SrcType) = generate(src)
}

object Sequential extends Generator[Int] {
  override def generate(count:Int) = new PersonGraph(tupler(count))
  def tupler(count:Int) : Iterable[Person] = tuplerFrom(1)(count)
  def tuplerFrom(start:Int)(count:Int) = {
    val res = Set[Person]()
    for (id <- start to (count+start-1)) res+=Person(id)
    res
  }
}

object Clique extends Generator[Int] {
  override def generate(count:Int) = PersonGraph( cliquer(Sequential.tupler(count)) )

  def cliquer(people:Iterable[Person]) = {
    people foreach((p1:Person)=>{
      for (p2 <- people filter(_ != p1)) p1 + Path(p2)
    })
    people
  }
    
}

object TwoGroupEachClique extends Generator[(Int,Int)] {
  override def generate(sizes:(Int,Int)) = PersonGraph(
    Clique.cliquer(Sequential.tuplerFrom(1)(sizes._1)) ++ Clique.cliquer(Sequential.tuplerFrom(sizes._1+1)(sizes._2))
  )
}
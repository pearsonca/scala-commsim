package edu.cap10.graph

import scala.actors._
import edu.cap10.person._
//import edu.cap10.channels._
import edu.cap10.sim._
import edu.cap10.message._
import edu.cap10.utils.DelegateIterable

//class PersonGraph(val delegate : Iterable[Person]) extends DelegateIterable[PersonGraph,Person] with Actor {
//  def +(that:PersonGraph) = new PersonGraph(this.delegate++that.delegate)
//  override def toString = {
//    val chan = (for (p<-this) yield { p+" :"+(p.channels mkString ",") }) mkString ",\n"
//    "People: [ "+this.delegate+" ]\nChannels :\n"+chan
//  }
//
//  override def act = loop {
//    react {
//      case SimTask("START",c) => {
//        foreach { _.start ! SimTask("START",c) }
//      }
//      case SimTask("STOP",c) => {
//        foreach { _ ! SimTask("STOP",c) }
//        exit
//      }
//      case task : SimTask => foreach { _ ! task }
//      case msg => println("Unhandled "+msg)
//    }
//  }
//  
//}
//
//object PersonGraph {
//  def apply(people:Iterable[Person]) = new PersonGraph(people)
//  implicit def people2PersonGraph(p:Set[Person]) = new PersonGraph(p)
//  implicit def loggerFactoryToSetter(lf:LoggerFactory) : (Path) => Unit = {
//    (p:Path) => { p.setLogger(lf.create(p)) }
//  }
//}
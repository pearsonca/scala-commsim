package edu.cap10.sim

import scala.actors.Actor
import edu.cap10.clock.Clock
import edu.cap10.person.Person
import edu.cap10.graph.generator._
import edu.cap10.message._

abstract class Simulation(size:Int) extends Actor {
	val clock = new Clock(size,this)
	val pg = Clique(size)
	val next = SimTask("NEXT",clock)
	val update = SimTask("UPDATE",clock)
	def sendNext() = pg.people foreach (_ ! next)
	def sendUp() = pg.people foreach (_ ! update)
	def act() = react {
	  case "START" =>
	    sendNext()
	    act()
	    // TODO make this into a start react loop or something? or just have it run by default when Sim is created?
	  case "NEXT" if sender == clock =>
	    sendUp()
	    sendNext()
	  case msg =>
	}
}

case class SimTask(val which: String, val clock:Clock)

object Simulation {
  def main(args: Array[String]) {
    val sim = new Simulation(5) {}
    sim.pg.setLogging(new FileLoggerFactory(sim.clock) )
    sim ! "START"
    sim ! "STOP"
  }
}
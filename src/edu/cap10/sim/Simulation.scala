package edu.cap10.sim

import scala.actors.Actor
import edu.cap10.clock._
import edu.cap10.person.Person
import edu.cap10.graph.generator._
import edu.cap10.message._

abstract class Simulation(size:Int) extends Actor {
	val clock = new Clock(size,this)
	val pg = Clique(size)
	val next = SimTask("NEXT",clock)
	val update = SimTask("UPDATE",clock)
	def sendNext = pg.people foreach { _ ! next }
	def sendUp = pg.people foreach { _ ! update }
	
	override def act = react {
	  case "START" =>
	    pg.people foreach { _.start }
	    sendNext
	    act
	  case Done(id) => clock ! Done(id)
	  case "NEXT" if sender == clock =>
	    sendUp
	    sendNext
	  case msg => sender ! msg
	}
}

case class SimTask(val which: String, val clock:Clock)

object Test {
  def main(args: Array[String]) {
    val sim = new Simulation(5) {}
    println("STARTING")
    println(sim.pg)
    //sim.pg.setLogging(new FileLoggerFactory(sim.clock) )
    val outerLoop = new Actor {
      override def act = while(true) {
        receive {
          case "START" => {
            sim.start
            sim ! "START"
          }
          case "STOP" if sender == sim => exit()
          case "STOP" => sim ! "STOP"
        }
      }
    }
    outerLoop.start
    outerLoop ! "START"
    outerLoop ! "STOP"
  }
}
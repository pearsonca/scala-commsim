package edu.cap10.sim

import scala.actors.Actor
import edu.cap10.clock._
import edu.cap10.person.Person
import edu.cap10.graph.generator._
import edu.cap10.message._

abstract class Simulation(size:Int) extends Actor {
	val clock = new Clock(size,this)
	val pg = Clique(size)
	val tasks = Task.map(clock)
//	val startTask = SimTask("START",clock)
//	val next = SimTask("NEXT",clock)
//	val update = SimTask("UPDATE",clock)
//	val stop = SimTask("STOP",clock)
	def send(which:String) = pg ! tasks(which)
	def startup = {
	  pg.start
	  send("START")
	}
	
	override def act = react {
	  case "START" =>
	    startup
	    act
	  case "NEXT" if sender == clock =>
	    sendUp
	    sendNext
	  case "STOP" => {
	    
	  }
	  case msg => println(msg)
	}
}

case class SimTask(val which: String, val clock:Clock)

object Task {
  val list = List("START", "COMMUNICATE","UPDATE","STOP")
  def map(clock:Clock) = {
    list map { task => (task, SimTask(task,clock)) } toMap
  }
}

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
          case "STOP" if sender == sim => {
            println("sim replied, shutting down")
            exit()
          }
          case "STOP" => sim ! "STOP"
        }
      }
    }
    outerLoop.start
    outerLoop ! "START"
    outerLoop ! "STOP"
  }
}
package edu.cap10.sim

import scala.actors.Actor
import edu.cap10.clock._
import edu.cap10.person.Person
import edu.cap10.graph.generators._
import edu.cap10.message._

//abstract class Simulation(size:Int) extends Actor {
//	val clock = new Clock(size,this)
//	val pg = Clique(size)
//	val tasks = Task.map(clock)
//	def send(which:String) = pg ! tasks(which)
//	def startup = {
//	  pg.start
//	  send("START")
//	}
//	
//	override def act = loop {
//	  react {
//		  case "START" =>
//		    startup
//		    this ! "COMMUNICATE"
//		  case "STOP" =>
//		    send("STOP")
//		    exit
//		  case "COMMUNICATE" => send("COMMUNICATE")
//		  case "UPDATE" => send("UPDATE")
//		  case msg => println(msg)
//		}
//	}
//}
//
//case class SimTask(val which: String, val clock:Clock)
//
//object Task {
//  val list = List("START", "COMMUNICATE","UPDATE","STOP")
//  def map(clock:Clock) = {
//    list map { task => (task, SimTask(task,clock)) } toMap
//  }
//}
//
//object Test {
//  def main(args: Array[String]) {
//    val sim = new Simulation(5) {}
//    println("STARTING")
//    println(sim.pg)
//    //sim.pg.setLogging(new FileLoggerFactory(sim.clock) )
//    sim.start ! "START" 
//    sim ! "STOP"
//  }
//}
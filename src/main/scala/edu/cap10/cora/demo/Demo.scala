package edu.cap10.cora.demo

import akka.actor.ActorSystem
import akka.actor.TypedActor
import akka.actor.TypedProps

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import edu.cap10.cora.Universe
import edu.cap10.cora.CSVLogger
import edu.cap10.util.Probability
import edu.cap10.util.Probability._

import scala.language.postfixOps

object SimulationProperties {
  val startDay = 0
  val (minAgents, maxAgents) = (10, 20)
  val sampleSize = 100
  val (minPlotPeriod, maxPlotPeriod) = (10, 30)
  val maxMeetLocations = MontrealProps.avgLocs - 1
}

object Demo {

  def main(args:Array[String]) : Unit = {
    val as = ActorSystem("Demo")
    val system = TypedActor(as)
    
    import MontrealProps._
    import SimulationProperties._  
        
    for (
      agentN <- minAgents to maxAgents by 2;
      plotPeriod <- maxPlotPeriod to minPlotPeriod by -10;
      meetLocs <- 1 to maxMeetLocations;
      agentP = dailyVisitProb - 1.0/plotPeriod;
      sample <- 1 to sampleSize;
      fname = f"./simdata/$agentN-$plotPeriod-$meetLocs-$sample%03d"
    ) {
      
        val fh = CSVLogger.makeFH(fname)
        val universe = system.typedActorOf(Universe.props(plotPeriod, agentN, uniqueLocs, meetLocs, agentP, avgLocs, fh))
        (startDay to totalDays) foreach { 
    	  t => Await.result( universe.tick(t), 1 seconds)
        }
        fh.flush(); fh.close()
        println("finished "+fname)
        system.poisonPill(universe)
        
    }
    
    as.shutdown
  }
}
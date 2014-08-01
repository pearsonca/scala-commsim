package edu.cap10.cora

import akka.actor.TypedActor
import akka.actor.TypedProps
import akka.actor.TypedActor.PreStart
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import java.io.BufferedWriter
import scala.util.Random.{shuffle, nextInt}

object Universe {
  def props(poissonRate: Double, groupSize: Int, locationCount:Int, meetingLocationCount:Int, agentVisProb:Double, avgLocs:Int, fh:BufferedWriter)
  	= TypedProps(classOf[TimeSensitive], new Universe(poissonRate, groupSize, locationCount, meetingLocationCount, agentVisProb, avgLocs, fh))
  def agentProps(id:Int, locs:Iterable[Int], p:Double, fh:BufferedWriter) = TypedProps(classOf[Agent], new AgentImpl(id, locs.toSeq, p, fh))

}

import scala.util._
/**
 * @param rate the Poisson rate for day of event
 */
class Universe(
    expectedDaysBetweenMeets:Double,
    groupSize: Int,
    locationCount:Int,
    meetingLocationCount:Int,
    visProb:Double,
    avgLocs:Int,
    val fh:BufferedWriter) 
  extends TimeSensitive with PoissonDraws with CSVLogger {
  
  val expectedK = expectedDaysBetweenMeets  // set the PoissonDraws parameter
  var daysToNextMeeting : Int = nextPoisson()    // get the initial days-to-next-covert meeting
  val meetingLocations = shuffle((0 to (locationCount-1))).take(meetingLocationCount)
  
  val agents : Seq[Agent] = {
    val sys = TypedActor.get(TypedActor.context)
    val srcLocs = (0 until locationCount) diff meetingLocations
    (1 to groupSize) map { id:Int =>
      val agentLocs = Random.shuffle(srcLocs).take(avgLocs - meetingLocationCount)
      sys.typedActorOf(Universe.agentProps(id, agentLocs ++ meetingLocations, visProb, fh), "agent"+id)
    }
  }
  
  override def resolve(when:Int) = {
    if (daysToNextMeeting < 0) {
      daysToNextMeeting = nextPoisson()
    } else {
      if (daysToNextMeeting == 0) {
        val loc = shuffle(meetingLocations).apply(0)
        val (h, m, s) = (nextInt(9)+8, nextInt(60), nextInt(60) ) 
                
        shuffle(agents).take(2).map( agent => agent.visit(loc, h, m, s ) ) foreach {
          response => {
            val s = Await.result(response, 400 millis)
            log(Seq(when, s))      
          }
        }
        
      }
      daysToNextMeeting -= 1
    }
    agents foreach { a => a.tick(when) }
    super.resolve(when)
  }
  
}
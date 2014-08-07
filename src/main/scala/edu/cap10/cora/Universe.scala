package edu.cap10.cora

import akka.actor.TypedActor
import akka.actor.TypedProps
import akka.actor.TypedActor.PreStart
import akka.actor.TypedActorFactory
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import java.io.BufferedWriter
import scala.util.Random.{shuffle, nextInt}
import scala.language.postfixOps

import edu.cap10.util.Probability
import edu.cap10.util.TimeStamp

object Universe {
  def props(poissonRate: Double, groupSize: Int, locationCount:Int, meetingLocationCount:Int, agentVisProb:Probability, avgLocs:Int, fh:BufferedWriter)
  	= TypedProps(classOf[TimeSensitive], new Universe(poissonRate, groupSize, locationCount, meetingLocationCount, agentVisProb, avgLocs, fh))

  def agent(id:Int, locs:Iterable[Int], p:Probability, fh:BufferedWriter)(implicit sys : TypedActorFactory) 
    = sys.typedActorOf(TypedProps(classOf[Agent], new AgentImpl(id, locs.toSeq, p, fh)), "agent"+id)

  def createAgents(agentCount:Int, locationCount:Int, meetingLocations:Seq[Int], avgLocs:Int, visProb:Probability, fh:BufferedWriter) : Seq[Agent] = {
    implicit val sys = TypedActor.get(TypedActor.context)
    val meetingLocationCount = meetingLocations.size
    val srcLocs = (0 until locationCount) diff meetingLocations
    (1 to agentCount) map { id:Int =>
      val agentLocs = shuffle(srcLocs).take(avgLocs - meetingLocationCount)
      agent(id, agentLocs ++ meetingLocations, visProb, fh)
    }
  }
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
    visProb:Probability,
    avgLocs:Int,
    val fh:BufferedWriter) 
  extends TimeSensitive with PoissonDraws with CSVLogger[TravelData] {
  
  val expectedK = expectedDaysBetweenMeets  // set the PoissonDraws parameter
  val meetingLocations = shuffle((0 to (locationCount-1))).take(meetingLocationCount)
  
  val agents : Seq[Agent]
    = Universe.createAgents(groupSize, locationCount, meetingLocations, avgLocs, visProb, fh)
  
  var daysToNextMeeting : Int = nextDraw()
    // get the initial days-to-next-covert meeting
  
  override def _tick(when:Int) = {

    if (daysToNextMeeting < 0) {
      daysToNextMeeting = nextDraw()
    } else {
      if (daysToNextMeeting == 0) { // time to meet
        val loc = shuffle(meetingLocations).apply(0)
        val ts = TimeStamp(nextInt(9)+8, nextInt(60), nextInt(60) )
        // choose place, time

        Await.result(
          Future.sequence(
            shuffle(agents).take(2).map( agent => agent.travel(loc, ts) )
            // randomly draw 2 agents to meet at a specific place, time
          ),
          400 millis
        ) foreach {
          res => log(res) // record results
        }

      }
      daysToNextMeeting -= 1
    }

    agents foreach { a => a.tick(when) }
    super._tick(when)
  }
  
}
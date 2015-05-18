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

import edu.cap10.util.{ Probability, TimeStamp }
import edu.cap10.util.TimeStamp._

object Universe {
  def props(poissonRate: Double, groupSize: Int, locationCount:Int, meetingLocationCount:Int, agentVisProb:Probability, avgLocs:Double, fh:BufferedWriter)
  	= TypedProps(classOf[TimeSensitive], new Universe(poissonRate, groupSize, locationCount, meetingLocationCount, agentVisProb, avgLocs, fh))

  def agent(id:Int, locs:Iterable[Int], p:Probability, fh:BufferedWriter)(implicit sys : TypedActorFactory) 
    = sys.typedActorOf(TypedProps(classOf[Agent], new AgentImpl(id, locs.toSeq, p, fh)), "agent"+id)

  def createAgents(agentCount:Int, locationCount:Int, meetingLocations:Seq[Int], avgLocs:Double, visProb:Probability, fh:BufferedWriter) : Seq[Agent] = {
    implicit val sys = TypedActor.get(TypedActor.context)
    val meetingLocationCount = meetingLocations.size
    val srcLocs = (0 until locationCount) diff meetingLocations
    (1 to agentCount) map { id:Int =>
      val agentLocs = shuffle(srcLocs).take((avgLocs - meetingLocationCount).toInt)
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
    avgLocs:Double,
    val fh:BufferedWriter) 
  extends TimeSensitive with PoissonDraws with CSVLogger[TravelData] {
  /* ... */
  // ...
  val expectedK = expectedDaysBetweenMeets  // set the PoissonDraws parameter
  val meetingLocations = shuffle((0 to (locationCount-1))).take(meetingLocationCount)
  
  val agents : Seq[Agent]
    = Universe.createAgents(groupSize, locationCount, meetingLocations, avgLocs, visProb, fh)
  
  var timeToNextMeeting : Int = nextDraw  
  def timeToMeet = timeToNextMeeting == 0
  
  implicit val rng = new Random
  
  override def _tick(when:Int) = {
    
    if (timeToMeet) {
      val place = shuffle(meetingLocations).apply(0)
      val time = TimeStamp(Hour(nextInt(9)+8), Minute.random, Second.random )
      Await.result(
        Future.sequence(
          shuffle(agents).take(2).map( agent => agent.travel(place, time) )
        ),
        400 millis
      ) foreach {
        res => log(res.copy( when = when ))
      }
      timeToNextMeeting = nextDraw
    } else {
      timeToNextMeeting -= 1
    }

    agents foreach { a => a.tick(when) }
    super._tick(when)
  }
  
}
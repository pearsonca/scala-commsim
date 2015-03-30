package edu.cap10.actormodels.demo

import scala.concurrent._
import ExecutionContext.Implicits.global
import edu.cap10.util.NaturalInt
import scala.util.Random
import edu.cap10.util.PoissonGenerator
import scala.language.implicitConversions
import scala.collection.Seq.{fill => replicate}
import scala.concurrent.duration._

object SimpleUniverse {
  def createAgents(seeds:Seq[Long], runConfig: SimpleParams, globalConfig : DataParams) : IndexedSeq[Dispatchable[TravelEvent]] = ???
}

class SimpleUniverse(
    runConfig : SimpleParams,
    globalConfig : DataParams
) extends TimeEvents[TravelEvent] {

  import runConfig._
  import globalConfig._
  import SimpleUniverse._

  val rng = new Random(seed)
  val gen = PoissonGenerator(rng, 1/meanMeetingFrequency)
  import rng.{ shuffle, nextLong => newSeed }
  import gen.{ next => nextMeeting }
  
  val meetingLocations = shuffle((0 to (uniqueLocations-1))).take(locationCount)
  val agents
    = createAgents(
     replicate(agentCount)( newSeed ),
     runConfig, globalConfig
  )
  

  
  var timeToNextMeeting : Int = nextMeeting
  def timeToMeet = timeToNextMeeting == 0

  override def _tick(when:Int) = {
    timeToNextMeeting = if (timeToMeet) {
      replicate(2)( 
          TravelEvent.random(-1, meetingLocations, meanVisitDuration.toInt) 
      ) zip(shuffle(agents)) foreach {
        case (event, agent) => agent.dispatch(event)
      }
      nextMeeting
    } else timeToNextMeeting - 1
    Await.result(Future.sequence(agents map {a => a.tick(when) }), Duration(1, SECONDS)).flatten ++ super._tick(when)
  }

}
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
  val meetingLocations = rng.shuffle((0 to (uniqueLocations-1))).take(locationCount)
  val agents
    = createAgents(
     Seq.fill(agentCount)( rng.nextLong ),
     runConfig, globalConfig
  )
  
  val gen = PoissonGenerator(rng, 1/meanMeetingFrequency)
  import gen.{ next => nextMeeting }
  
  var timeToNextMeeting : Int = nextMeeting
  var day : Int = 0
  def timeToMeet = timeToNextMeeting == 0

  override def _tick(when:Int) = {

    if (timeToMeet) {
      replicate(2)( 
          TravelEvent.random(-1, meetingLocations, meanVisitDuration.toInt) 
      ).zip(rng.shuffle(agents)) foreach {
        case (event, agent) => agent.dispatch(event)
      }
      timeToNextMeeting = nextMeeting
    } else {
      timeToNextMeeting -= 1
    }
    val res = Await.result(Future.sequence(agents map {a => a.tick(when) }), Duration(1, SECONDS)).flatten
    day += 1
    super._tick(when) ++ res
  }

}
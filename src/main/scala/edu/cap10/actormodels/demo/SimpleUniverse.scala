package edu.cap10.actormodels.demo

import scala.concurrent._
import ExecutionContext.Implicits.global

class SimpleUniverse(
    runConfig : SimConfig,
    globalConfig : ReferenceConfig
) extends TimeEvents[TravelEvent] {


  import runConfig._
  import globalConfig._

  val expectedK = 1/meanMeetingFrequency  // set the PoissonDraws parameter
  val meetingLocations = shuffle((0 to (uniqueLocs-1))).take(meanLocationCount.toInt)

  val agents
    = SimUniverse.createAgents(meanAgentCount.toInt, meetingLocations, runConfig, globalConfig)

  var timeToNextMeeting : Int = nextDraw
  var day : Int = 0
  def timeToMeet = timeToNextMeeting == 0

  override def _tick(when:Int) = {

    if (timeToMeet) {
      val numberMeeting = 2
      val durSecs = (nextDouble*meanMeetingDuration*2*60).toInt
      val refEvent = TravelEvent.random(-1, meetingLocations, day, durSecs)
      val tes = Seq.fill(numberMeeting)(refEvent)
      val pairs = shuffle(agents).take(numberMeeting).zip(tes)
      pairs foreach { case (agent, te) => agent.dispatch(te) }
      timeToNextMeeting = nextDraw
    } else {
      timeToNextMeeting -= 1
    }
    val res = Await.result(Future.sequence(agents map {a => a.tick(when) }), Duration(1, SECONDS)).flatten
    day += 1
    super._tick(when) ++ res
  }

}
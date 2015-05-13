package edu.cap10.actormodels.demo

import scala.concurrent._
import ExecutionContext.Implicits.global
import edu.cap10.util.NaturalInt
import edu.cap10.util.Probability
import edu.cap10.util.Probability._
import edu.cap10.util.LocalRNG
import scala.util.Random
import edu.cap10.util.PoissonGenerator
import scala.language.implicitConversions
import scala.collection.Seq.{fill => replicate}
import scala.concurrent.duration._
import akka.actor.{ TypedActor, TypedActorFactory, TypedProps}

object SimpleUniverse {
  
  def props(
    runConfig : SimpleParams,
    globalConfig : DataParams
  ) = TypedProps(classOf[TimeEvents[TravelEvent]], new SimpleUniverse(runConfig.seed, runConfig, globalConfig))
    
  def createAgents(
    count:NaturalInt,
    runConfig: SimpleParams,
    globalConfig : DataParams
  )(
    implicit rng : Random,
    sys : TypedActorFactory = TypedActor.get(TypedActor.context)
  ) : Seq[Dispatchable[TravelEvent]] = {
    import runConfig._, globalConfig._
    val hauntGen = HauntGenerator(rng, 1 to uniqueLocations).uniform
    (1 to count) map { id => 
      sys.typedActorOf(
        SimpleAgent.props(
            id, hauntGen(meanVisitedLocations.toInt), runConfig, globalConfig, rng.nextLong
        ),
        "agent"+id
      )
    }
  }
}

class SimpleUniverse(
    override val seed : Long,
    runConfig : SimpleParams,
    globalConfig : DataParams
) extends TimeEvents[TravelEvent] with LocalRNG {

  import runConfig.{seed => _, _}, globalConfig._, SimpleUniverse._

  val meetingGenerator = PoissonGenerator(meanMeetingPeriod)
  import rng.shuffle, meetingGenerator.{ next => daysToNextMeeting }
  
  val meetingLocations = HauntGenerator(rng, 1 to uniqueLocations).uniform(locationCount)
  val agents = createAgents(agentCount, runConfig, globalConfig)
  
  var timeToNextMeeting : Int = daysToNextMeeting
  def timeToMeet : Boolean = timeToNextMeeting == 0
  
  override def tick(when:Int) = {
    var dispatch : Future[Seq[Unit]] = Future.successful(Seq())
    
    timeToNextMeeting = if (timeToMeet) {
      val te = TravelEvent.random(-1, meetingLocations, meanVisitDuration.toInt)
      dispatch = Future.sequence(replicate(2)(te) zip(shuffle(agents)) map {
        case (event, agent) => agent.dispatch(event)
      })
      daysToNextMeeting
    } else timeToNextMeeting - 1
    for (
      _ <- dispatch; // await dispatch success
      agentActivity <- Future.sequence(agents map { _.tick(when) })
    ) yield agentActivity.flatten
  }

}
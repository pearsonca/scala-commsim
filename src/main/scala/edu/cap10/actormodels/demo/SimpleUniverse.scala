package edu.cap10.actormodels.demo

import scala.concurrent._
import ExecutionContext.Implicits.global
import edu.cap10.util.NaturalInt
import edu.cap10.util.Probability
import edu.cap10.util.Probability._
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
  ) = TypedProps(classOf[TimeEvents[TravelEvent]], new SimpleUniverse(runConfig, globalConfig))
  
  def createAgent(
    id : AgentID,
    haunts : Seq[LocationID],
    dailyVisitProb: Probability,
    meanVisitDuration: Double,
    seed : Long
  )(implicit sys : TypedActorFactory)
    = sys.typedActorOf(
      TypedProps(classOf[Dispatchable[TravelEvent]], 
        new SimpleAgent(id, haunts, dailyVisitProb, 1, seed)), "agent"+id
      )
  
  def createAgents(
    seeds:Seq[Long],
    runConfig: SimpleParams,
    globalConfig : DataParams
  )(implicit rng : Random) : Seq[Dispatchable[TravelEvent]] = {
    implicit val sys = TypedActor.get(TypedActor.context)
    import runConfig._
    import globalConfig._
    seeds.zipWithIndex map { case (agentSeed, id) => 
      val haunts = rng.shuffle((0 to (uniqueLocations-1))).take(meanVisitedLocations.toInt)
      createAgent(id, haunts, dailyVisitProb, meanVisitDuration, seed)
    }
  }
}

class SimpleUniverse(
    runConfig : SimpleParams,
    globalConfig : DataParams
) extends TimeEvents[TravelEvent] {

  import runConfig._
  import globalConfig._
  import SimpleUniverse._

  implicit val rng = new Random(seed)
  val gen = PoissonGenerator(1/meanMeetingFrequency)
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

  override def tick(when:Int) = {
    timeToNextMeeting = if (timeToMeet) {
      replicate(2)( 
          TravelEvent.random(-1, meetingLocations, meanVisitDuration.toInt) 
      ) zip(shuffle(agents)) foreach {
        case (event, agent) => agent.dispatch(event)
      }
      nextMeeting
    } else timeToNextMeeting - 1
    
    for (res <- Future.sequence(agents map { a => a.tick(when) })) yield res.flatten
  }

}
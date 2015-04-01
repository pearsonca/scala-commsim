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

object StupidUniverse {
  
  def props(
    runConfig : SimpleParams,
    globalConfig : DataParams
  ) = TypedProps(classOf[TimeEvents[TravelEvent]], new StupidUniverse(runConfig, globalConfig))
  
  def createAgent(
    id : AgentID,
    haunts : Seq[LocationID],
    dailyVisitProb: Probability,
    meanVisitDuration: Double,
    seed : Long
  )(implicit sys : TypedActorFactory)
    = sys.typedActorOf(
      TypedProps(classOf[Dispatchable[TravelEvent]], 
        new StupidAgent(id, haunts, dailyVisitProb, 1, seed)), "agent"+id
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
      val haunts = (0 to (uniqueLocations-1)).take(meanVisitedLocations.toInt)
      createAgent(id, haunts, dailyVisitProb, meanVisitDuration, seed)
    }
  }
}

class StupidUniverse(
    runConfig : SimpleParams,
    globalConfig : DataParams
) extends TimeEvents[TravelEvent] {

  import runConfig._
  import globalConfig._
  import StupidUniverse._

  implicit val rng = new Random(seed)
  val gen = PoissonGenerator(meanMeetingPeriod)
  import rng.{ shuffle, nextLong => newSeed }
  import gen.{ next => nextMeeting }
  
  val meetingLocations = (0 to (uniqueLocations-1)).drop(meanVisitedLocations.toInt).take(locationCount)
  val agents
    = createAgents(
     replicate(agentCount)( newSeed ),
     runConfig, globalConfig
    )
  
  var timeToNextMeeting : Int = nextMeeting
  def timeToMeet = timeToNextMeeting == 0

  override def tick(when:Int) = {
    var dispatch : Future[Seq[Unit]] = Future.successful(Seq())
    
    timeToNextMeeting = if (timeToMeet) {
      val te = TravelEvent.random(-1, meetingLocations, meanVisitDuration.toInt)
      dispatch = Future.sequence(replicate(2)(te) zip(agents) map {
        case (event, agent) => {
          agent.dispatch(event)
        }
      })
      nextMeeting
    } else timeToNextMeeting - 1
    for (
      _ <- dispatch;
      res <- Future.sequence(agents map { a => a.tick(when) })
    ) yield res.flatten
  }

}
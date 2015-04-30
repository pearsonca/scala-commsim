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

//object SimpleUniverse {
//  
//  def props(
//    runConfig : SimpleParams,
//    globalConfig : DataParams
//  ) = TypedProps(classOf[TimeEvents[TravelEvent]], new SimpleUniverse(runConfig.seed, runConfig, globalConfig))
//  
//  def createAgent(
//    id : AgentID,
//    haunts : Seq[LocationID],
//    dailyVisitProb: Probability,
//    meanVisitDuration: Double,
//    seed : Long
//  )(implicit sys : TypedActorFactory)
//    = sys.typedActorOf(
//      TypedProps(classOf[Dispatchable[TravelEvent]], 
//        new SimpleAgent(id, haunts, dailyVisitProb, 1, seed)), "agent"+id
//      )
//  
//  def createAgents(
//    seeds:Seq[Long],
//    runConfig: SimpleParams,
//    globalConfig : DataParams
//  )(implicit rng : Random) : Seq[Dispatchable[TravelEvent]] = {
//    implicit val sys = TypedActor.get(TypedActor.context)
//    import runConfig._
//    import globalConfig._
//    seeds.zipWithIndex map { case (agentSeed, id) => 
//      val haunts = rng.shuffle(1 to uniqueLocations).take(meanVisitedLocations.toInt)
//      createAgent(id, haunts, dailyVisitProb, meanVisitDuration, seed)
//    }
//  }
//}

class SubgroupingUniverse(
    override val seed : Long,
    runConfig : SimpleParams,
    globalConfig : DataParams
) extends TimeEvents[TravelEvent] with LocalRNG {

  import runConfig.{seed => _, _}, globalConfig._//, SimpleUniverse._

  val meetingGenerator = PoissonGenerator(meanMeetingPeriod)
  import rng.{ shuffle, nextLong => newSeed }
  import meetingGenerator.{ next => daysToNextMeeting }
  
  val meetingLocations = shuffle((0 to (uniqueLocations-1))).take(locationCount)
  val agents : Seq[Dispatchable[TravelEvent]] = ???
//  createAgents(
//    replicate(agentCount)( newSeed ),
//    runConfig, globalConfig
//  )
  
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
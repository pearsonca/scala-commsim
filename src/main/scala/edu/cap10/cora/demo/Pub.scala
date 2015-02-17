package edu.cap10.cora.demo

case class SimConfig(agentN:Int = 10, meetLocations:Int = 2, expectedDaysBetweenMeets : Double = 20, meetDuration : Double = 20) {
  def parse(args:List[String]) : Option[SimConfig] = args match {
    case Nil => Some(this)
    case "-n"::n::rest => this.copy(agentN=n.toInt).parse(rest)
    case "-l"::l::rest => this.copy(meetLocations=l.toInt).parse(rest)
    case "-t"::t::rest => this.copy(expectedDaysBetweenMeets=t.toDouble).parse(rest)
    case "-d"::d::rest => this.copy(meetDuration=d.toDouble).parse(rest)
    case other => None
  }
}

import akka.actor.ActorSystem
import akka.actor.TypedActor
import akka.actor.TypedActorFactory
import akka.actor.TypedProps
import scala.util.Random.{shuffle, nextInt}
import scala.concurrent._
import scala.concurrent.duration._

import edu.cap10.cora.{TimeEvents, PoissonDraws, Dispatchable}
import edu.cap10.util.{Probability, TimeStamp}

case class TravelEvent(agentId : Int, locId : Int, timeIn : Long, timeOut : Long)

class AgentImpl(id:Int, haunts:Seq[Int], p:Probability) extends Dispatchable[TravelEvent] {
  override def _dispatch(te:TravelEvent) = super._dispatch(te.copy(agentId = id)) // update events to *my* id
}

object SimUniverse {
  def props(
    runConfig : SimConfig,
    globalConfig : ReferenceConfig
  ) = TypedProps(classOf[TimeEvents[TravelEvent]], new SimUniverse(runConfig, globalConfig))

  def agent(id:Int, locs:Iterable[Int], p:Probability)(implicit sys : TypedActorFactory) 
    = sys.typedActorOf(TypedProps(classOf[Dispatchable[TravelEvent]], new AgentImpl(id, locs.toSeq, p)), "agent"+id)

  def createAgents(agentCount:Int, locationCount:Int, meetingLocations:Seq[Int], avgLocs:Double, visProb:Probability) : Seq[Dispatchable[TravelEvent]] = {
    implicit val sys = TypedActor.get(TypedActor.context)
    val meetingLocationCount = meetingLocations.size
    val srcLocs = (0 until locationCount) diff meetingLocations
    (1 to agentCount) map { id:Int => {
      val drawCount = 1 // TODO use avgLocs to draw a number of visited locations
      val agentLocs = shuffle(srcLocs).take(drawCount)
      agent(id, agentLocs ++ meetingLocations, visProb)
    }}
  }
}

class SimUniverse(
//    expectedDaysBetweenMeets:Double,
//    groupSize: Int,
//    locationCount:Int,
//    meetingLocationCount:Int,
//    visProb:Probability,
//    avgLocs:Double
    runConfig : SimConfig,
    globalConfig : ReferenceConfig
) 
  extends TimeEvents[TravelEvent] with PoissonDraws {
  
  import ExecutionContext.Implicits.global
  import runConfig._
  import globalConfig._

  val expectedK = expectedDaysBetweenMeets  // set the PoissonDraws parameter
  val meetingLocations = shuffle((0 to (uniqueLocs-1))).take(meetLocations)
  
  val agents
    = SimUniverse.createAgents(agentN, uniqueLocs, meetingLocations, avgLocs, dailyVisitProb)
  
  var timeToNextMeeting : Int = nextDraw
  var day : Int = 0
  def timeToMeet = timeToNextMeeting == 0
  
  override def _tick(when:Int) = {
    
    if (timeToMeet) {
      val place = shuffle(meetingLocations).apply(0)
      val time = TimeStamp(nextInt(9)+8, nextInt(60), nextInt(60) )
      val numberMeeting = 2
      val tes = for (i <- 1 to numberMeeting) yield TravelEvent(-1, place, time+day, time+day)
      val pairs = shuffle(agents).take(numberMeeting).zip(tes)
      Await.result(
        Future.sequence(
          pairs.map( { case (agent, te) => agent.dispatch(te) })
        ),
        400 millis
      )
      timeToNextMeeting = nextDraw
    } else {
      timeToNextMeeting -= 1
    }

    agents foreach { a => a.tick(when) }
    day += 1
    super._tick(when)
  }
  
}

case class SimSystem(runConfig : SimConfig, globalConfig : ReferenceConfig) {
  val as = ActorSystem("")
  val system = TypedActor(as)
  val universe = system.typedActorOf(SimUniverse.props(runConfig, globalConfig))
  def shutdown = {
    system.poisonPill(universe)
    as.shutdown()
  }
}

object Pub extends App {

  val usage = """
Usage: pub [-n int] [-l int] [-t num] [-d num]
   -n: the number of agents
   -l: the number of covert meeting locations
   -t: inter-plot meeting period (days)
   -d: average meeting duration (hours)
"""

  SimConfig().parse(args.toList) match {
    case Some(config) => println(config)
    case None => println(usage)
  }

}
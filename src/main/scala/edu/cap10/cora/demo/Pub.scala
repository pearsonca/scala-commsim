package edu.cap10.cora.demo

/* A SimConfig collects the parameters needed for the synthetic covert behavior.
 *
 * Defines defaults + a `parse` method for handling command line argument
 */
case class SimConfig (
  meanAgentCount : Double = 10,
  meanLocationCount : Double = 2,
  meanMeetingFrequency : Double = 1/20, // days
  meanMeetingDuration : Double = 20, // minutes
  meanExtraParticipants : Double = 0, // agents beyond the minimum 2
  seed : Long = 0
) {
  // TODO use regex to extract flag + val, consolidate `...parse(rest)`
  // val flag = """-(\w)""".r
  def parse(args:List[String]) : Option[SimConfig] = args match {
    case Nil => Some(this)
    case "-n"::n :: rest => copy(meanAgentCount = n.toDouble).parse(rest)
    case "-l"::l :: rest => copy(meanLocationCount = l.toDouble).parse(rest)
    case "-t"::t :: rest => copy(meanMeetingFrequency = t.toDouble).parse(rest)
    case "-d"::d :: rest => copy(meanMeetingDuration = d.toDouble).parse(rest)
    case "-s"::s :: rest => copy(seed = s.toLong).parse(rest)
    case other => None
  }
}

import akka.actor.ActorSystem
import akka.actor.TypedActor
import akka.actor.TypedActorFactory
import akka.actor.TypedProps
import scala.util.Random.{shuffle, nextInt, nextDouble}
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.language.implicitConversions

import edu.cap10.cora.{TimeEvents, PoissonDraws, Dispatchable}
import edu.cap10.util.{Probability, TimeStamp}

object TravelEvent {
  def random(
    agentId: Int,
    locations: Seq[Int],
    day:Int,
    durationSeconds:Int,
    minhour: Int = 8,
    maxhour:Int = 16
  ) : TravelEvent = {
    val start = TimeStamp(nextInt(maxhour-minhour)+minhour, nextInt(60), nextInt(60) )+day
    TravelEvent(agentId, shuffle(locations).head, start, durationSeconds)
  }
  
  
}

case class TravelEvent(agentId : Int, locId : Int, timeStart:Long, durationSeconds : Int)

object CovertAgent {
  
  trait ArrivesEarly extends Dispatchable[TravelEvent] {
    def _howearly(te:TravelEvent) : Int = ??? // calculate early arrival time
    override def _dispatch(te:TravelEvent) = {
      super._dispatch(te.copy(timeStart = te.timeStart - _howearly(te)))
    }
  }
  
  trait StaysLate extends Dispatchable[TravelEvent] {
    def _howlate(te:TravelEvent) : Int = ??? // calculate stay time
    override def _dispatch(te:TravelEvent) = {
      super._dispatch(te.copy(durationSeconds = te.durationSeconds + _howlate(te)))
    }
  }
  
}

abstract class CovertAgent(id:Int, simConfig: SimConfig, globalConfig: ReferenceConfig) extends Dispatchable[TravelEvent] {

  import SimConfig._
  import ReferenceConfig._
  
  override def _dispatch(te:TravelEvent) = {
    // dispatched event - by default we can just update the id and use the exact event
    // for more complex behavior, we can use CovertAgent.{ArrivesEarly, StaysLate}
    // TODO validate / deconflict receiving multiple dispatches in a day
    super._dispatch(te.copy(agentId = id))
  }
  
  
  def _tripsToday(when:Int) : Seq[TravelEvent] = {
    val timeSlots : List[(Int,Int)] = ??? // available time slots for trips
    _makeTrips(timeSlots)
  }
  
  def _makeTrips(timeSlots : List[(Int,Int)], acc:List[TravelEvent] = Nil) : List[TravelEvent] =
    timeSlots match {
      case Nil => acc
      case (start, end) :: rest => _makeTrip(start, end, acc.size) match {
        case Some(event) => {
          val newWindowStart : Int = ??? // TODO event.timeStart + event.durationSeconds
          _makeTrips((newWindowStart, end) :: rest, event :: acc)
        }
        case None => _makeTrips(rest, acc)
      }
    }
  
  def _makeTrip(start : Int, end : Int, tripCount : Int) : Option[TravelEvent] = {
    // decide if to make a trip in this slot based on time of day
    //  amount of time available for trip, and trips so far today
    //  if taking a trip, so create it and produce Some[TravelEvent]
    //  else return None
    ???
  }

  
  
//      if (_dispatched || (p < nextDouble)) { // if I already have directions or I'm not going out today
//      super._tick(when)
//    } else {
//      val durSecs = (nextDouble*meetDuration*2*60).toInt
//      super._tick(when) :+ TravelEvent.random(id, haunts, when, durSecs)
//    }

  
  override def _tick(when:Int) = {
    super._tick(when) ++ _tripsToday(when)
  }
  
}

//class AgentImpl(id:Int, haunts:Seq[Int], p:Probability, meetDuration:Double) extends Dispatchable[TravelEvent] {
//  override def _dispatch(te:TravelEvent) = {
//    val extraSeconds = (nextDouble*5*2*60).toInt; // TODO distribute
//    val myte = te.copy(agentId = id, durationSeconds = te.durationSeconds+extraSeconds)
//    super._dispatch(if (nextDouble < 0.5) {
//      myte.copy(timeStart = myte.timeStart - extraSeconds)
//    } else {
//      myte
//    })
//  }
//
//  override def _tick(when:Int) = {
//    if (_dispatched || (p < nextDouble)) { // if I already have directions or I'm not going out today
//      super._tick(when)
//    } else {
//      val durSecs = (nextDouble*meetDuration*2*60).toInt
//      super._tick(when) :+ TravelEvent.random(id, haunts, when, durSecs)
//    }
//  }
//}


class AgentImpl(id:Int, haunts:Seq[Int], p:Probability, meetDuration:Double) extends Dispatchable[TravelEvent] {
  override def _dispatch(te:TravelEvent) = {
    val extraSeconds = (nextDouble*5*2*60).toInt; // TODO distribute
    val myte = te.copy(agentId = id, durationSeconds = te.durationSeconds+extraSeconds)
    super._dispatch(if (nextDouble < 0.5) {
      myte.copy(timeStart = myte.timeStart - extraSeconds)
    } else {
      myte
    })
  }

  override def _tick(when:Int) = {
    if (_dispatched || (p < nextDouble)) { // if I already have directions or I'm not going out today
      super._tick(when)
    } else {
      val durSecs = (nextDouble*meetDuration*2*60).toInt
      super._tick(when) :+ TravelEvent.random(id, haunts, when, durSecs)
    }
  }
}

object SimUniverse {
  def props(
    runConfig : SimConfig,
    globalConfig : ReferenceConfig
  ) = TypedProps(classOf[TimeEvents[TravelEvent]], new SimUniverse(runConfig, globalConfig))

  def agent(id:Int, locs:Iterable[Int], p:Probability, meetDuration:Double)(implicit sys : TypedActorFactory)
    = sys.typedActorOf(TypedProps(classOf[Dispatchable[TravelEvent]], new AgentImpl(id, locs.toSeq, p, meetDuration)), "agent"+id)

  def createAgents(agentCount:Int, locationCount:Int, meetingLocations:Seq[Int], avgLocs:Double, visProb:Probability, avgMeetDuration:Double) : Seq[Dispatchable[TravelEvent]] = {
    implicit val sys = TypedActor.get(TypedActor.context)
    val meetingLocationCount = meetingLocations.size
    val srcLocs = (0 until locationCount) diff meetingLocations
    (1 to agentCount) map { id:Int => {
      val drawCount = 1 // TODO use avgLocs to draw a number of visited locations
      val agentLocs = shuffle(srcLocs).take(drawCount)
      agent(id, agentLocs ++ meetingLocations, visProb, avgMeetDuration)
    }}
  }
}

abstract class Universe (
    runConfig : SimConfig,
    globalConfig : ReferenceConfig
) extends TimeEvents[TravelEvent] {
  
  type LocationID = Int
  
  def _createLocations : IndexedSeq[LocationID] = ??? // draw process for locations
  val meetingLocations = _createLocations
  
  def _createAgents : Seq[Dispatchable[TravelEvent]] = {
    val agentCount : Int = ??? // draw process for how many agents
    SimUniverse.createAgents(agentCount, meetingLocations, runConfig, globalConfig)
  }
  
  val agents = _createAgents

}

class SimUniverse(
    runConfig : SimConfig,
    globalConfig : ReferenceConfig
)
  extends TimeEvents[TravelEvent] with PoissonDraws {

  import ExecutionContext.Implicits.global
  import runConfig._
  import globalConfig._

  val expectedK = 1/meanMeetingFrequency  // set the PoissonDraws parameter
  val meetingLocations = shuffle((0 to (uniqueLocs-1))).take(meanLocationCount.toInt)

  val agents
    = SimUniverse.createAgents(meanAgentCount.toInt, uniqueLocs, meetingLocations, avgLocs, dailyVisitProb, meanMeetingDuration)

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

case class SimSystem(runConfig : SimConfig, globalConfig : ReferenceConfig) {
  val as = ActorSystem("SimulationSystem")
  val system = TypedActor(as)
  val universe = system.typedActorOf(SimUniverse.props(runConfig, globalConfig))

  def run = {
    val res = for (t <- 1 to globalConfig.totalDays) yield Await.result( universe.tick(t), Duration(1, SECONDS))
    res.flatten
  }
  def shutdown = as.shutdown
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
    case Some(config) => {
      val sim = SimSystem(config, MontrealProps)
      val results = sim.run
      results map { case TravelEvent(who, where, when, howLong) => println(f"$who, $where, $when, ${when+howLong}") }
      sim.shutdown
    }
    case None => println(usage)
  }

}

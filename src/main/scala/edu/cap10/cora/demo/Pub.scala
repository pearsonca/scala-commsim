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
import scala.util.Random.{shuffle, nextInt, nextDouble}
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.language.implicitConversions

import edu.cap10.cora.{TimeEvents, PoissonDraws, Dispatchable}
import edu.cap10.util.{Probability, TimeStamp}

object TravelEvent {
  def random(agentId: Int, locations: Seq[Int], day:Int, durationSeconds:Int, minhour: Int = 8, maxhour:Int = 16) : TravelEvent = {
    val start = TimeStamp(nextInt(maxhour-minhour+1)+minhour, nextInt(60), nextInt(60) )+day
    TravelEvent(agentId, shuffle(locations).head, start, durationSeconds)
  }
}

case class TravelEvent(agentId : Int, locId : Int, timeStart:Long, durationSeconds : Int)

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
    if (_dispatched || (nextDouble > p)) { // if I already have directions or I'm not going out today
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

class SimUniverse(
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
    = SimUniverse.createAgents(agentN, uniqueLocs, meetingLocations, avgLocs, dailyVisitProb, meetDuration)
  
  var timeToNextMeeting : Int = nextDraw
  var day : Int = 0
  def timeToMeet = timeToNextMeeting == 0
  
  override def _tick(when:Int) = {
    
    if (timeToMeet) {
      val numberMeeting = 2
      val durSecs = (nextDouble*meetDuration*2*60).toInt      
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
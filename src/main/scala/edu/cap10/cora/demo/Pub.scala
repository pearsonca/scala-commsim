package edu.cap10.cora.demo

case class SimConfig(agentN:Int = 10, meetLocations:Int = 2, plotPeriod : Double = 20, meetDuration : Double = 20) {
  def parse(args:List[String]) : Option[SimConfig] = args match {
    case Nil => Some(this)
    case "-n"::n::rest => this.copy(agentN=n.toInt).parse(rest)
    case "-l"::l::rest => this.copy(meetLocations=l.toInt).parse(rest)
    case "-t"::t::rest => this.copy(plotPeriod=t.toDouble).parse(rest)
    case "-d"::d::rest => this.copy(meetDuration=d.toDouble).parse(rest)
    case other => None
  }
}

import akka.actor.ActorSystem
import akka.actor.TypedActor
import akka.actor.TypedActorFactory
import akka.actor.TypedProps
import scala.util.Random.{shuffle, nextInt}

import edu.cap10.cora.{TimeSensitive, PoissonDraws}
import edu.cap10.util.Probability

object SimUniverse {
  def props(poissonRate: Double, groupSize: Int, locationCount:Int, meetingLocationCount:Int, agentVisProb:Probability, avgLocs:Int)
    = TypedProps(classOf[TimeSensitive], new SimUniverse(poissonRate, groupSize, locationCount, meetingLocationCount, agentVisProb, avgLocs))

  def agent(id:Int, locs:Iterable[Int], p:Probability)(implicit sys : TypedActorFactory) 
    = sys.typedActorOf(TypedProps(classOf[Agent], new AgentImpl(id, locs.toSeq, p)), "agent"+id)

  def createAgents(agentCount:Int, locationCount:Int, meetingLocations:Seq[Int], avgLocs:Int, visProb:Probability) : Seq[Agent] = {
    implicit val sys = TypedActor.get(TypedActor.context)
    val meetingLocationCount = meetingLocations.size
    val srcLocs = (0 until locationCount) diff meetingLocations
    (1 to agentCount) map { id:Int =>
      val agentLocs = shuffle(srcLocs).take(avgLocs - meetingLocationCount)
      agent(id, agentLocs ++ meetingLocations, visProb)
    }
  }
}

class SimUniverse(
    expectedDaysBetweenMeets:Double,
    groupSize: Int,
    locationCount:Int,
    meetingLocationCount:Int,
    visProb:Probability,
    avgLocs:Double) 
  extends TimeSensitive with PoissonDraws {

  val expectedK = expectedDaysBetweenMeets  // set the PoissonDraws parameter
  val meetingLocations = shuffle((0 to (locationCount-1))).take(meetingLocationCount)
  
  val agents : Seq[Agent]
    = SimUniverse.createAgents(groupSize, locationCount, meetingLocations, avgLocs, visProb)
  
  var timeToNextMeeting : Int = nextDraw  
  def timeToMeet = timeToNextMeeting == 0
  
  override def _tick(when:Int) = {
    
    if (timeToMeet) {
      val place = shuffle(meetingLocations).apply(0)
      val time = TimeStamp(nextInt(9)+8, nextInt(60), nextInt(60) )
      Await.result(
        Future.sequence(
          shuffle(agents).take(2).map( agent => agent.travel(place, time) )
        ),
        400 millis
      ) foreach {
        res => log(res.copy( when = when ))
      }
      timeToNextMeeting = nextDraw
    } else {
      timeToNextMeeting -= 1
    }

    agents foreach { a => a.tick(when) }
    super._tick(when)
  }
  
}

case class SimSystem(plotPeriod : Double, agentN : Int, uniqueLocs : Int, meetLocs : Int, agentP : Probability, avgLocs : Double) {
  val as = ActorSystem("")
  val system = TypedActor(as)
  val universe = system.typedActorOf(SimUniverse.props(plotPeriod, agentN, uniqueLocs, meetLocs, agentP, avgLocs))
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
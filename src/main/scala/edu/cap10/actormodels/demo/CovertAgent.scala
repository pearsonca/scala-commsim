package edu.cap10.actormodels.demo

import edu.cap10.util.LocalRNG
import edu.cap10.util.Probability
import edu.cap10.util.Probability._
import edu.cap10.util.TimeStamp
import edu.cap10.util.TimeStamp._
import scala.languageFeature.implicitConversions

trait CovertAgent extends Dispatchable[TravelEvent] with LocalRNG {

  protected def id : AgentID 
  
  final override def _dispatch(te:TravelEvent) = 
    super._dispatch( dispatchModifier(te).copy(agentID = id) )

  final override def _tick(when:Int) =
    timesGenerator(locationSelector) ++: super._tick(when)
    
  protected def timesGenerator(locations: Seq[LocationID]) : Seq[TravelEvent]
  
  protected def dispatchModifier(te:TravelEvent) : TravelEvent
  
  protected def locationSelector : Seq[LocationID]
  
  
}

trait SimpleLocations extends CovertAgent {
  
  protected def haunts : Seq[LocationID]
  protected def dailyVisitP : Probability
  
  // choose uniformly from locations
  override def locationSelector =
    if (!_dispatched && (rng.nextProbability < dailyVisitP)) {
      Seq(haunts(rng.nextInt(haunts.size)))
    } else Seq()
    
}

trait FiveToNine extends CovertAgent {
  
  protected def meanVisitDuration : Double
  
  override protected def timesGenerator(locations : Seq[LocationID]): Seq[TravelEvent] =
    locations map { (locID:LocationID) =>
      TravelEvent(
        id, locID,
        TimeStamp(Hour(5+rng.nextInt(21-5+1)), Minute.random, Second.random),
        meanVisitDuration.toInt
      )
    }    
}

trait Obedient extends CovertAgent {
  override def dispatchModifier(te:TravelEvent) : TravelEvent = te
}

trait EarlyBird extends CovertAgent {
  override def dispatchModifier(te:TravelEvent) : TravelEvent = te
}

trait Lingerer extends CovertAgent {
  override def dispatchModifier(te:TravelEvent) : TravelEvent = te
}
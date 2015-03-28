package edu.cap10.actormodels.demo

import edu.cap10.util.TimeStamp
import scala.util.Random._
import edu.cap10.util.NaturalInt
import scala.languageFeature.implicitConversions

case class DataEvent private (uID: AgentID, locationID: LocationID, start:Long, end:Long) {
  override def toString = f"$uID $locationID $start $end"
}

object DataEvent {
  def generatorDay(refTime:Long = 0) : Function2[Int,TravelEvent,DataEvent] = 
    (day:Int, te:TravelEvent) => 
      DataEvent(te.agentID, te.locationId, te.timeStart+day + refTime, te.timeStart+day+te.durationSeconds + refTime)
}

object TravelEvent {
  def random(
    agentID: AgentID,
    locations: Seq[LocationID],
    durationSeconds : NaturalInt,
    minhour : NaturalInt = 6,
    maxhour : NaturalInt = 17
  ) = {
    assert(!locations.isEmpty, "locations is empty")
    assert(maxhour <= 24, "max hour exceeds day length")
    assert(minhour <= maxhour, "max hour exceeds day length")
    
    TravelEvent(
      agentID, shuffle(locations).head,
      TimeStamp(
        nextInt(maxhour-minhour)+minhour,
        nextInt(60), nextInt(60)
      ), durationSeconds
    )
  }
  
}

case class TravelEvent (
    agentID : AgentID,
    locationId : LocationID,
    timeStart: TimeStamp,
    durationSeconds : Int
)

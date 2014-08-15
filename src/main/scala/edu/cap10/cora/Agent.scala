package edu.cap10.cora

import akka.actor.TypedActor
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.{shuffle, nextInt}
import java.io.BufferedWriter

import edu.cap10.util.Probability
import edu.cap10.util.TimeStamp

case class TravelData(when:Int, id: Int, location:Int, ts:TimeStamp) extends CSVLoggable {
  def mkString(sep : String = ", ") : String = Seq(when, id, location, ts) mkString sep
}

trait Agent extends TimeSensitive with Travels[TravelData] with CSVLogger[TravelData]

class AgentImpl(
  val id:Int, val normLocs:Seq[Int],
  val visProbPerTick:Probability, val fh:BufferedWriter) extends Agent {
  /* ... */
  // ...
  private def randomLocation = shuffle(normLocs).apply(0)
  
  private def randomHour = nextInt(9)+8
  
  override def travelResult(location: Int, ts: TimeStamp) =
    TravelData(-1, id, location, ts)
   
  override def _tick(when:Int) = {
    if (!_traveled && (Math.random < visProbPerTick)) {
      log( _travel( location = randomLocation, ts = randomHour ).copy( when = when ) )
    } else {
      _clearTravel
    }
    super._tick(when)
  }
}
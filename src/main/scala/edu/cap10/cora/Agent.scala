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

class AgentImpl(val id:Int, val normLocs:Seq[Int], val visProbPerTick:Probability, val fh:BufferedWriter) extends Agent {
  
  var visited : Boolean = false
  
  override def randomLocation = shuffle(normLocs).apply(0)
  
  private def randomHour = nextInt(9)+8
  
  override def _travel(location: =>Int = randomLocation, ts: TimeStamp) = {
    visited = true
    TravelData(-1, id, location, ts)
  }
  
  override def _tick(when:Int) = {
    if (!visited && (Math.random < visProbPerTick)) { // making own trip if not directed to take one
      log( _travel( ts = randomHour ).copy( when = when ) )
    } else {
      visited = false
    }
    super._tick(when)
  }
}
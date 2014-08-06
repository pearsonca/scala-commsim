package edu.cap10.cora

import akka.actor.TypedActor
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.{shuffle, nextInt}
import java.io.BufferedWriter

import edu.cap10.util.Probability
import edu.cap10.util.TimeStamp


trait Agent extends TimeSensitive with Travels with CSVLogger

class AgentImpl(val id:Int, val normLocs:Seq[Int], val visProbPerTick:Probability, val fh:BufferedWriter) extends Agent {
  
  var visited : Boolean = false
  
  override def _travel(location:Int, hour:Int, min: =>Int = nextInt(60), sec: =>Int = nextInt(60)) = {
    visited = true
    toRow(Seq(id, location, TimeStamp(hour, min, sec)))
  }
  
  override def _tick(when:Int) = {
    if (!visited && (Math.random < visProbPerTick)) { // making own trip if not directed to take one
      log(Seq(when, _travel(shuffle(normLocs).apply(0), nextInt(9)+8 )))
    } else {
      visited = false
    }
    super._tick(when)
  }
}
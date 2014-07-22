package edu.cap10.cora

import akka.actor.TypedActor
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.{shuffle, nextInt}
import java.io.BufferedWriter

trait GoesPlaces {
  def visit(location:Int, hour:Int, min: =>Int = nextInt(60), sec: =>Int = nextInt(60)) = Future { _visit(location, hour, min, sec)  }
  
  protected[this] def _visit(location:Int, hour:Int, min: =>Int = nextInt(60), sec: =>Int = nextInt(60)) = 
    f"$location "+timestamp(hour, min, sec)
    
  def timestamp(hour:Int, min: Int, sec: Int) = f"$hour%02d:$min%02d:$sec%02d"
}

trait Agent extends TimeSensitive with GoesPlaces with CSVLogger

class AgentImpl(val id:Int, val normLocs:Seq[Int], val visProbPerDay:Double, val fh:BufferedWriter) extends Agent {
  
  var visited : Boolean = false
  
  override def _visit(location:Int, hour:Int, min: =>Int = nextInt(60), sec: =>Int = nextInt(60)) = {
    visited = true
    toRow(Seq(id, location, timestamp(hour, min, sec)))
  }
  
  override def resolve(when:Int) = {
    if (!visited && Math.random() < visProbPerDay) { // making own trip if not directed to take one
      log(Seq(when, _visit(shuffle(normLocs).apply(0), nextInt(9)+8 )))
    } else {
      visited = false
    }
    super.resolve(when)
  }
}
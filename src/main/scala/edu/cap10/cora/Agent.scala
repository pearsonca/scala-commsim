package edu.cap10.cora

import akka.actor.TypedActor
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random
import java.io.BufferedWriter

trait GoesPlaces {
  def visit(location:Int, hour:Int, min: =>Int = Random.nextInt(60), sec: =>Int = Random.nextInt(60)) = Future { _visit(location, hour, min, sec)  }
  protected[this] def _visit(location:Int, hour:Int, min: =>Int = Random.nextInt(60), sec: =>Int = Random.nextInt(60)) = f"$location $hour%02d:$min%02d:$sec%02d"
}

trait Agent extends TimeSensitive with GoesPlaces

class AgentImpl(val id:Int, val normLocs:Seq[Int], val visProbPerDay:Double, fh:BufferedWriter) extends Agent {
  
  var visited : Boolean = false
  
  override def visit(location:Int, hour:Int, min: =>Int = Random.nextInt(60), sec: =>Int = Random.nextInt(60)) = {
    visited = true
    super.visit(location, hour, min, sec).map( res => id + " " +res)
  }
  
  override def resolve(when:Int) = {
    if (!visited && Math.random() < visProbPerDay) {
      fh.write(when+ " " + id + " " + _visit(Random.shuffle(normLocs).apply(0), Random.nextInt(9)+8 )+"\n")
    } else {
      visited = false
    }
    super.resolve(when)
  }
}
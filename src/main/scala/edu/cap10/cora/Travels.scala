package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.nextInt
import edu.cap10.util.TimeStamp

import scala.language.implicitConversions 

trait Travels[ResultType] {
  
  protected[this] def randomLocation : Int
  
  implicit def hour2RandomTimeStamp(hour:Int) = TimeStamp(hour, nextInt(60), nextInt(60))
  
  def travel(
    location: =>Int = randomLocation,
    ts: TimeStamp
  ) = Future { _travel(location, ts)  }
  
  protected[this] def _travel(
    location: =>Int = randomLocation,
    ts: TimeStamp
  ) : ResultType

}
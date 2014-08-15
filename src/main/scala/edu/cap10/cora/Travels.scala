package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global

import edu.cap10.util.TimeStamp

trait Travels[ResultType] {
  
  private[this] var traveled : Boolean = false
  def _traveled = traveled
  def _clearTravel = traveled = false
  def travelResult(location:Int, ts:TimeStamp) : ResultType
    
  // ...
  def travel(
    location: Int,
    ts: TimeStamp
  ) = Future { _travel(location, ts)  }
  
  protected[this] def _travel(
    location: Int,
    ts: TimeStamp
  ) : ResultType = {
    traveled = true
    travelResult(location, ts)
  }

}
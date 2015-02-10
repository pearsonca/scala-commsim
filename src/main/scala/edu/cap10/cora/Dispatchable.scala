package edu.cap10.cora

import scala.concurrent._
import scala.collection.mutable.Queue
import ExecutionContext.Implicits.global

import edu.cap10.util.TimeStamp

trait Dispatchable[EventType] extends TimeEvents[EventType] {
  
  private[this] val q : Queue[EventType] = Queue()
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
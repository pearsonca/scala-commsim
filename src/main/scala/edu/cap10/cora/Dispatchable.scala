package edu.cap10.cora

import scala.concurrent._
import scala.collection.mutable.Queue
import ExecutionContext.Implicits.global

import edu.cap10.util.TimeStamp

trait Dispatchable[EventType] extends TimeEvents[EventType] {
  
  private[this] val q : Queue[EventType] = Queue()
  private[this] def dispatched : Boolean = !q.isEmpty
  def _dispatched = dispatched

  def _dispatch(e: EventType) : Boolean = {
   val ref = q.size 
   q += e
   q.size + 1 == q.size
  }
  def dispatch(e:EventType) = Future { _dispatch(e) }
  
  override def _tick(when:Int) : List[EventType] = {
    val res = super._tick(when) ++ q
    q.clear()
    res
  }
  
}
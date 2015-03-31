package edu.cap10.actormodels.demo

import scala.concurrent._
import scala.collection.mutable.Queue
import ExecutionContext.Implicits.global

trait Dispatchable[EventType] extends TimeEvents[EventType] {
  
  private[this] val q = Queue[EventType]()
  protected[this] def _dispatched = !q.isEmpty

  protected[this] def _dispatch(e: EventType) : Boolean = {
   val ref = q.size 
   q += e
   q.size + 1 == q.size
  }
  def dispatch(e:EventType) = Future { _dispatch(e) }
  
  override def _tick(when:Int) = {
    val res = super._tick(when) ++ q
    q.clear()
    res
  }
} 

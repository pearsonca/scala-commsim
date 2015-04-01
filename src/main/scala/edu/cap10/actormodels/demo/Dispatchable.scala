package edu.cap10.actormodels.demo

import scala.concurrent._
import ExecutionContext.Implicits.global

trait Dispatchable[EventType] extends TimeEvents[EventType] {
  
  private var q : Seq[EventType] = Seq()
  protected def _dispatched = !q.isEmpty

  protected def _dispatch(e: EventType) : Unit = {
   q = q :+ e
  }
  def dispatch(e:EventType) = Future { _dispatch(e) }
  
  override def _tick(when:Int) = {
    //System.err.println(f"on $when, q size " + q.size)
    q ++ super._tick(when)
  }
  
  override def tick(when:Int) = super.tick(when) andThen { 
    case res =>
      q = Seq()
      res
  }
} 

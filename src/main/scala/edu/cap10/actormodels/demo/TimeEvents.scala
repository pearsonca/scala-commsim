package edu.cap10.actormodels.demo

import scala.concurrent._
import ExecutionContext.Implicits.global

trait TimeEvents[ResponseType] {

  final def tick(when:Int) : Future[Seq[ResponseType]] = Future({ _tick(when) })
  
  protected[this] def _tick(when:Int) : Seq[ResponseType] = Seq()

}

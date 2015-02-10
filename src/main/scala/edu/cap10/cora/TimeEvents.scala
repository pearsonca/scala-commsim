package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

trait TimeEvents[+ResponseType] {
  // take care of generating the exterior method
  final def tick(when:Int) : Future[List[ResponseType]] = Future({ _tick(when) })
  
  // internal agent method returns empty list (i.e., does notthing) in response to ticks
  protected[this] def _tick(when:Int) : List[ResponseType] = List()

}

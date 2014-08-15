package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

trait TimeSensitive {
  // ...
  final def tick(when:Int) : Future[Int] = Future({ _tick(when) })
    
  protected[this] def _tick(when:Int) = when

}

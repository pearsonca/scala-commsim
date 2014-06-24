package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global

trait Age extends TimeSensitive {

  protected[this] var age = 0
  
  override protected def resolve(when:Int) = {
    age += when - last
    super.resolve(when)
  }
  
  def askAge : Future[Int] = Future { age }
  
}
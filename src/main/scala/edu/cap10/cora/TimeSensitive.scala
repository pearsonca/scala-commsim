package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

trait Reply

object Ack extends Reply
case class Error(msg:String) extends Reply

trait TimeSensitive {
  
  private[this] var was = 0
  protected def last = was
  
  final def tick(when:Int) : Future[Try[Reply]] =
    Future({ resolve(when) }).andThen({
      case Success(res) =>
        was = when
        res
      case f => f
    })

    
  protected def resolve(when:Int) : Try[Reply] =
    Success(Ack)

}

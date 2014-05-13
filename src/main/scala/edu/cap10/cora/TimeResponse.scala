package edu.cap10.cora

import scala.concurrent.Future.successful
import scala.concurrent.Promise
import akka.actor.TypedActor
import scala.util.Try
import scala.util.Success

trait TimeResponse {
  
  final def tick(when:Int) = {
    implicit val promise = Promise[Reply]()
    resolve(when)
    promise.future
  }
  
  protected def resolve(when:Int, reply:Try[Reply] = Success(Ack))(implicit promise:Promise[Reply]) = {
    promise complete reply
  }

}

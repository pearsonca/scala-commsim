package edu.cap10.cora

import scala.concurrent.Future
import akka.actor.TypedActor
import akka.actor.TypedProps
import akka.actor.ActorRef

trait Reply

object Ack extends Reply
case class Error(msg:String) extends Reply

trait StackingAgentBehavior {
  protected implicit def executionContext = TypedActor.context.dispatcher
  
  def tick(implicit when:Int) : Future[Reply] = Future.successful(Ack)
  /* for any traits that respond to ticks
   * override def tick(implicit when:Int) = {
   *   ... trait specific behavior ...
   *   super.tick
   * }
   **/
  
}

/* TODO: allow developers to just write:
 * tick = {
 *   ... trait specific behavior, possibly depending on when
 * }
 * and have macro/sbt rewrite it to include boilerplate
**/
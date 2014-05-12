package edu.cap10.cora

import scala.concurrent.Future.successful
import akka.actor.TypedActor

trait TimeResponse {
  protected implicit final def executionContext = TypedActor.context.dispatcher

  def tick(implicit when:Int) = successful(Ack)
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

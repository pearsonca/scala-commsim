package edu.cap10.cora

import akka.actor.TypedActor

trait Agent {
  protected implicit final def executionContext = TypedActor.context.dispatcher
  protected final def universe = TypedActor.context.parent
}
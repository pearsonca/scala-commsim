package edu.cap10.cora

import akka.actor.Actor
import akka.actor.ActorRef

abstract class Behavior extends Actor {

  def agent = context.parent
  
  def next : ActorRef
  def prev : ActorRef
  
  def propagate(msg:Any) = next forward msg
  
  override def unhandled(msg:Any) = this propagate msg
  
}

case class Unhandled(msg:Any)

case class End(var prev : ActorRef) extends Behavior {
  val next = agent
  
  def receive = { 
    case m => propagate(Unhandled(m))
  }
  
}
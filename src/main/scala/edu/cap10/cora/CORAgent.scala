package edu.cap10.cora

import akka.actor.Actor
import akka.actor.ActorRef

abstract class CORALink extends Actor {
  def prev : ActorRef
  def next : ActorRef
  
  override def receive = root
  
  def root : Receive
  def become( behavior:Receive, discardOld:Boolean = true) = context become( root orElse behavior, discardOld )
  def unbecome = context unbecome
  
  def propagate(msg:Any) = next.tell(msg, sender)
  
  override def unhandled(msg:Any) = next forward msg
}



//object Front extends CORALink {
//  def prev = this
//  def next = this
//  def receive = {
//    case _ =>
//  }
//}
//
//object End extends CORALink {
//  def prev = this
//  def next = this
//  def receive = {
//    case _ =>
//  }
//}

import akka.actor.Status._
import akka.actor.Stash

abstract class CORAgent extends Actor with Stash {
  
  def head : CORALink
  def last : CORALink
  
  def receive = running
  
  def running : Receive
  def waiting(ID : AnyRef) : Receive = {
    case Success(ID) =>
      unstashAll()
      context unbecome()
    case msg => stash()
  }
  
}
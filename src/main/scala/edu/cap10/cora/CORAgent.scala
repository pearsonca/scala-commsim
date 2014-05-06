package edu.cap10.cora

// want to have agents
// with behavior that is independently specified (therefore independently testable, developed, etc)
// that doesn't interfere
// but can be used together
// that behavior should be adaptable w/o having to recreate thing (again, non-interfering

import akka.actor.TypedActor
import scala.concurrent.{ Future, future }

import akka.actor.ActorRef

trait HasFamily {
  def head 
}

//abstract class CORALink extends TypedActor.Receiver {
//  def prev : ActorRef
//  def next : ActorRef
//  
//  override def receive = root
//  
//  def root : Receive
//  def become( behavior:Receive, discardOld:Boolean = true) = context become( root orElse behavior, discardOld )
//  def unbecome = context unbecome
//  
//  def propagate(msg:Any) = next.tell(msg, sender)
//  
//  override def unhandled(msg:Any) = next forward msg
//}



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

//abstract class CORAgent extends Actor with Stash {
//  
//  def head : CORALink
//  def last : CORALink
//  
//  def receive = running
//  
//  def running : Receive
//  def waiting(ID : AnyRef) : Receive = {
//    case Success(ID) =>
//      unstashAll()
//      context unbecome()
//    case msg => stash()
//  }
//  
//}
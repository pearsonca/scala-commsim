package edu.cap10.cora.proc

import akka.actor.ActorRef
import akka.actor.TypedActor
import akka.actor.TypedProps
import scala.collection.mutable.{Set => MSet}
import scala.concurrent.Future

import edu.cap10.cora._

trait FamilyRelations extends StackingAgentBehavior {

  implicit def ec = TypedActor.context.dispatcher
  
  var parents : Set[FamilyRelations] = Set()
  var children : Set[FamilyRelations] = Set()
  
  def addParent(p:FamilyRelations) = {
    Future {
      if (!parents(p)){
        parents = parents + p
        Ack
      }
      else Error("Parents already contains "+p)
    }
  }
  
  def addChild(c:FamilyRelations) = {
    Future {
      if (!children(c)){
        children = children + c
        Ack
      }
      else Error("Parents already contains "+c)
    }
  }
  
  def getChildren = Future { children }
  def getParents = Future { parents }
  
}
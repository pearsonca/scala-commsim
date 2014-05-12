package edu.cap10.cora.proc

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

import edu.cap10.cora._

trait FamilyRelations extends TimeResponse {
  
  private[this] var parents  : Set[FamilyRelations] = Set()
  private[this] var children : Set[FamilyRelations] = Set()
  private[this] var partners : Set[FamilyRelations] = Set()
  
  private[this] def nbSiblings = {
    val kids = parents.map { _.getChildren }
    Future.reduce(kids){ _ ++ _ }
  }
  private[this] def siblings = Await.result(nbSiblings, 1 second)
  
  
  def addParent(p:FamilyRelations) = {
    Future {
      if (!parents(p)) {
        parents = parents + p
        Ack
      }
      else Error("Parents already contains "+p)
    }
  }
  
  def addChild(c:FamilyRelations) = {
    Future {
      if (!children(c)) {
        children = children + c
        Ack
      }
      else Error("Children already contains "+c)
    }
  }
  
  def getChildren = Future { children }
  def getParents = Future { parents }
  
}
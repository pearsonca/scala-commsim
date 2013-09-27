package edu.cap10.moneyentities

import collection.mutable.SortedSet

case class Firm(pId:Long, employees:SortedSet[MoneyEntity], override val logger:MoneyEntityLogger) extends MoneyEntity(pId) {

  val edges = Map( Relationship.INFLUENCE -> employees )
  
}
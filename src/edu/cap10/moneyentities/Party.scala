package edu.cap10.moneyentities

import collection.mutable.SortedSet

class Party(pId:Long, override val logger : MoneyEntityLogger) extends MoneyEntity(pId) {

  val edges = Map(
    Relationship.INFLUENCE -> SortedSet[MoneyEntity]() // Individuals    
  )
  
}
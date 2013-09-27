package edu.cap10.moneyentities

import edu.cap10.graph.Vertex
import edu.cap10.sim._

trait MoneyEntityLogger extends Logger[(Donation.Value,MoneyEntity),MoneyEntity]

abstract class MoneyEntity(val id:Long) extends Vertex[Relationship.Value, MoneyEntity] 
with SimulationActor[Long,(Donation.Value,MoneyEntity)] {

}

object Relationship extends Enumeration {
  val EMPLOYMENT, PREFERENCE, MEMBERSHIP, INFLUENCE = Value
}
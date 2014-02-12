package edu.cap10
import akka.actor.ActorRef

package object simactor {
  type Time = Long
  type People = Set[ActorRef]
  type Sim = ActorRef
  def empty[A] = Set.empty[A]
  implicit class Probability(p:Double) {
    require(0d<=p,"Probabilities must be >= 0")
    require(p<=1d,"Probabilities must be <= 1")
  }

  def random = Math.random
}
package edu.cap10.actormodels.demo

import scala.util.Random

case class HauntGenerator(rng : Random) {

  def uniform(locations : Seq[Int]) : (Int) => Seq[Int] = {
    require(locations.size > 0, "Cannot sample from non-existent locations.")
    (count : Int) => {
      require(count >= 0, "Cannot request a negative location count.")
      require(count <= locations.size, "Asked for more locations than exist.")
      rng.shuffle(locations).take(count)
    } ensuring(_.size == count)
  }
 
  def distributed(locations: Seq[Int])(mean:Double) : Seq[Int] = ???
  
}
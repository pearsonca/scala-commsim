package edu.cap10.actormodels.demo

import scala.util.Random

case class HauntGenerator(rng : Random) {

  def basic(locations : Seq[Int])(count:Int) : Seq[Int] = ???
  
  def distributed(locations: Seq[Int])(mean:Double) : Seq[Int] = ???
  
}
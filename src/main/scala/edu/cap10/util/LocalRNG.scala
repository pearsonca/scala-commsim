package edu.cap10.util

import scala.util.Random

trait LocalRNG {

  def seed : Long
  implicit protected lazy val rng = new Random(seed)
  
}
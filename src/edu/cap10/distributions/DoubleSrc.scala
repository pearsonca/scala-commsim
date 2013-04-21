package edu.cap10.distributions

import scala.util.Random.nextDouble

object DoubleSrc extends DistroSrc[Double] {
  val inner = new Iterator[Double] {
    val hasNext = true
    def next = nextDouble
  }
}
package edu.cap10.distributions

import scala.util.Random.nextInt

object IntSrc extends DistroSrc[Int] {
  val inner = new Iterator[Int] {
    val hasNext = true
    def next = nextInt
  }
}
package edu.cap10.distributions

abstract class BinomialSrc extends DistroSrc[Int]

object BinomialSrc {
  def apply(max:Int, p:Double) = {
    require(max > 0,"max arg must be greater than 0.")
    require(p >= 0 && p <= 1,"p must be a probability (0 <= p <= 1).")
  }
}
package edu.cap10.util

import scala.language.implicitConversions

object Probability {
  
  def apply(p:Double) = {
    if (0 <= p && p <= 1) new Probability(p)
    else throw new ArithmeticException(f"p = $p%f is not a valid probability.")
  }
  
  implicit def convertDtoP(p:Double) : Probability = this(p)
  implicit def convertPtoD(p:Probability) : Double = p.underlying
  implicit def ordering[P <: Probability] : Ordering[Probability] = Ordering[Double].on(p => p.underlying)
  
  implicit class PRNG(rng : scala.util.Random) {
    def next : Probability = rng.nextDouble()
  }

  val TRUE = this(1)
  val FALSE = this(0)
  
}



class Probability private (val underlying: Double) extends AnyVal {
  def +(p:Probability) = Probability(underlying + p.underlying)
}

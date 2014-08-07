package edu.cap10.util

import scala.language.implicitConversions

object Probability {
  
  def apply(p:Double) = {
    if (0 <= p && p <= 1) new Probability(p)
    throw new ArithmeticException(f"p = $p%f is not a valid probability.")
  }
  
  implicit def convertDtoP(p:Double) : Probability = this(p)
  implicit def convertPtoD(p:Probability) : Double = p.underlying

}

class Probability private (val underlying: Double) extends AnyVal {
  def -(p:Double) = Probability(underlying - p)
}
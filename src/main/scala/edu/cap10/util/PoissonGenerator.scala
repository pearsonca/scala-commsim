package edu.cap10.util

import scala.util.Random
import scala.language.implicitConversions
import edu.cap10.util.Probability._
import edu.cap10.util.NaturalInt._

case class PoissonGenerator (rng: PRNG, mean: Double) {
  
  private[this] val cdf : Stream[Probability] = {
    def loop(last:Probability, k:NaturalInt) : Stream[Probability] = {
      val v : Probability = mean*last / k
      v #:: loop(v, k+1)
    } 
    val base = Math.exp(-mean)
    base #:: loop(base, 1)
  }.scanLeft(FALSE)(_ + _).drop(1)
  
  def next : NaturalInt = cdf.indexWhere(rng.next < _)
    
}
package edu.cap10.util

import scala.language.implicitConversions

object NaturalInt {
  
  def apply(i:Int) = {
    if (0 <= i) new NaturalInt(i)
    else throw new ArithmeticException(f"p = $i is not a valid natural number.")
  }
  
  implicit def convertItoN(i:Int) = this(i)
  implicit def convertNtoI(n:NaturalInt) = n.underlying
  implicit def ordering[NI <: NaturalInt] : Ordering[NI] = Ordering[Int].on(n => n.underlying)
  
}

class NaturalInt private (val underlying: Int) extends AnyVal {
  def +(n:NaturalInt) = new NaturalInt(underlying + n.underlying)
  override def toString = f"NaturalInt($underlying)"
}
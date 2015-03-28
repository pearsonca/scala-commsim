package edu.cap10.util

import scala.language.implicitConversions

object NaturalInt {
  
  def apply(i:Int) = {
    if (0 <= i) new NaturalInt(i)
    else throw new ArithmeticException(f"p = $i is not a valid natural number.")
  }
  
  implicit def convertItoN(i:Int) = this(i)
  implicit def convertNtoI(n:NaturalInt) = n.underlying
  implicit def ordering : Ordering[NaturalInt] = Ordering[Int].on(n => n.underlying)
  
}

class NaturalInt private (val underlying: Int) extends AnyVal
package edu.cap10.util

import scala.util.Random
import scala.language.implicitConversions

case class TimeStamp(hour:Hour, min:Minute, sec:Second) {
    
  override val toString = f"$hour:$min:$sec"
 
  def +(day:Int) = day*24*60*60 + toSecs
  def toSecs = hour.toSecs + min.toSecs + sec.toSecs
  
}

object Hour {
  def apply(h:Int) = {
    require(0 <= h && h <= 23, f"$h%02d is not between 0 and 23")
    new Hour(h)
  }
}

class Hour private (val h:Int) extends AnyVal {
  def toSecs = h*60*60
}

object Minute {
  def apply(m:Int) = {
    require(0 <= m && m <= 59, f"$m%02d is not between 0 and 59")
    new Minute(m)
  }
  
  def random(implicit rng : Random) = apply(rng.nextInt(60))
}

class Minute private (val m:Int) extends AnyVal {
  def toSecs = m*60
}

object Second {
  def apply(s:Int) = {
    require(0 <= s && s <= 59, f"$s%02d is not between 0 and 59")
    new Second(s)
  }
  def random(implicit rng : Random) = apply(rng.nextInt(60))
}

class Second private (val s:Int) extends AnyVal {
  def toSecs = s
}
// TODO value to re-writing as
//  TimeStamp(hh:Hour, mm:Minute, ss:Second) and defining those classes
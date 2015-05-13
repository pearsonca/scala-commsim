package edu.cap10.util

case class TimeStamp(hour:Int, min:Int, sec:Int) {

  require(0 <= hour && hour < 24, f"hour $hour must be [0,24).")
  require(0 <= min && min < 60, f"min $min must be [0,60).")
  require(0 <= sec && sec < 60, f"sec $sec must be [0,60).")
  
  override val toString = f"$hour%02d:$min%02d:$sec%02d"
 
  def +(day:Int) : Long = day*24*60*60 + toSec
  def toSec : Long = hour*60*60 + min*60 + sec
  
}

// TODO value to re-writing as
//  TimeStamp(hh:Hour, mm:Minute, ss:Second) and defining those classes
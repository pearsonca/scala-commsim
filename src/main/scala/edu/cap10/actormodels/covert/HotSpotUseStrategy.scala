package edu.cap10.actormodels.covert

import scala.util.Random.nextInt

trait HotSpotUseStrategy {
  def plan(day:Day, options:Set[HotSpot]) : Seq[AccessPlan]
}

trait StartTimeStrategy {
  def start(day:Day, hs:HotSpot, otherEvents:List[AccessPlan]) : Time
}

trait EndTimeStrategy {
  def end(day:Day, hs:HotSpot, start:Time, otherEvents:List[AccessPlan]) : Time
}

case class OnePerDay(s:StartTimeStrategy, e:EndTimeStrategy) extends HotSpotUseStrategy {
  import s.start
  import e.end
  override def plan(day:Day, options:Set[HotSpot]) = {
    val which = nextInt(options.size)
    val hs = options.drop(which).head
    val sTime = start(day, hs, List.empty)
    val eTime = end(day, hs, sTime, List.empty)
    Seq((hs, sTime, eTime))
  }
}
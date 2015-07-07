package edu.cap10.actormodels.covert

import scala.util.Random.nextDouble
import scala.collection.mutable.{Set => MSet}

case class Provocateur(id:UserID, acceptNewHotSpot:Double) {

  private var _clock : Day = 0
  
  def clock : Day = _clock
  
  def tick(time:Day) : Boolean = {
    _clock += 1
    true
  }
  
  private val _hotspots = MSet[HotSpot]()
  
  def aware(hotspot:HotSpot) =
    if (nextDouble() < acceptNewHotSpot)
      _hotspots add hotspot
    else
      false
  
  def hotspots : Set[HotSpot] = _hotspots.toSet
  
}
package edu.cap10.actormodels.covert

import scala.collection.mutable.{
  Map => MMap,
  MutableList => MList
}

sealed trait HotSpotState
case object NotYetActive extends HotSpotState
case object Active extends HotSpotState
case object NoLongerActive extends HotSpotState

case class HotSpot(
  id:HotSpotID, activation:Day, shutdown:Day,
  startHourDistro: CDF = Array.empty
) {
  
  private var _clock : Day = 0
  private var _state : HotSpotState = if (activation == 0) Active else NotYetActive
  
  def clock : Day = _clock
  def state : HotSpotState = _state
  
  def tick(time:Day) : HotSpotState = {
    _clock += 1
    if (_clock == activation)   _setState(Active)
    else if (_clock > shutdown) _setState(NoLongerActive)
    _state 
  }
  
  private def _setState(s : HotSpotState) = {
    _state = s
  }
  
  private val _cache : MMap[Long, Long] = MMap() 
  
  def login(userID:Long, time:Long) = if (_state == Active) {
    if (_cache contains userID)
      false
    else
      _cache.put(userID, time).isEmpty
  } else {
    false
  }
    
  private val _record : MList[AccessRecord] = MList()
  
  def logout(userID:Long, time:Long) = _cache remove userID match {
    case Some(loginTime) => if (loginTime <= time) {
        _record += ((_clock, userID, loginTime, time))
        true
      } else false
    case None => false
  }
  
  def record(query:AccessRecord) = _record.contains(query)
  
  def replay = _record.toList
  
}
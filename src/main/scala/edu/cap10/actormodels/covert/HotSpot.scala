package edu.cap10.actormodels.covert

import scala.collection.mutable.{Map => MMap}

sealed trait HotSpotState
case object NotYetActive extends HotSpotState
case object Active extends HotSpotState
case object NoLongerActive extends HotSpotState

case class HotSpot(id:Long, logName:String, activationDate:Long, shutdownDate:Long) {
  
  private var _clock : Long = 0
  private var _state : HotSpotState = if (activationDate == 0) Active else NotYetActive
  
  def clock : Long = _clock
  
  def tick(time:Long) : HotSpotState = {
    _clock += 1
    if (_clock == activationDate)    _setState(Active)
    else if (_clock == shutdownDate) _setState(NoLongerActive)
    _state 
  }

  private def _record(userID:Long, timeIn:Long, timeOut:Long) : Boolean = {
    true
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
  
  def logout(userID:Long, time:Long) = _cache remove userID match {
    case Some(loginTime) => true
    case None => false
  }
  
  // receive logins, logouts
    // reject if not active
    // if active, queue
    // once login matched with logout, record
  
  // activate
    // open log file
  
  // deactivate
    // flush+close log file
}
package edu.cap10.actormodels.covert

sealed trait HotSpotState
case object NotYetActive extends HotSpotState
case object Active extends HotSpotState
case object NoLongerActive extends HotSpotState

case class HotSpot(id:Long, logName:String, activationDate:Long, shutdownDate:Long) {
  
  private var _clock = 0
  private var _state = if (activationDate == 0) NotYetActive else Active
  
  def clock : Long = _clock
  
  def tick(time:Long) : HotSpotState = {
    _clock += 1
    _state 
  }
  // receive ticks
    // once tick == activationDate, activate
    // once tick == shutdownDate, deactive
  
  // receive logins, logouts
    // reject if not active
    // if active, queue
    // once login matched with logout, record
  
  // activate
    // open log file
  
  // deactivate
    // flush+close log file
}
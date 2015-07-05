package edu.cap10.actormodels.covert

case class Provocateur(id:UserID) {

  private var _clock : Day = 0
  
  def clock : Day = _clock
  
  def tick(time:Day) : Boolean = {
    _clock += 1
    true
  }
  
}
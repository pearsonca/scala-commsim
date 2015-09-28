package edu.cap10.actormodels.covert

import breeze.stats.distributions.Gamma

case class LoginEvent(user_id:UserID, location_id:HotSpotID, login:Long, logout:Long)

/**
 * @author cap10
 */
case class SynthUser(id:Int, shape:Double, mean:Double, 
    locations:Array[Location], prefCDF:Array[Double]) {
  // draw next event day
  // on event day, draw number of events
  // if meeting day, replace events with meeting
  
  val gen = Gamma(shape, mean/shape)
  val searchSrc = scala.collection.Searching.search(prefCDF)
  val rng = new scala.util.Random(id)

  var tilEvent : Int = -1
  var meeting : Option[Location] = None
  def meetAt(l:Location) = { meeting = Some(l) }
  
  def tick = {
    if (tilEvent == -1) {
      // draw new time tilEvent
    } else {
      if (tilEvent == 0 || !meeting.isEmpty) {
        
      }
    }
  }
  
  def draw = {
    val t = gen.draw()
    val day = (t/(24*60*60)).round // days til next event
    val loc = locations(searchSrc.search(rng.nextDouble).insertionPoint)
    val event = loc.draw
    
    LoginEvent(id, loc.id, event.startDaySecs, event.endDaySecs)
  }
}
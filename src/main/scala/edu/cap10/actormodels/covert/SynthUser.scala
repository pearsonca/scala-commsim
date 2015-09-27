package edu.cap10.actormodels.covert

import breeze.stats.distributions.Gamma

case class LoginEvent(user_id:UserID, location_id:HotSpotID, login:Long, logout:Long)

/**
 * @author cap10
 */
case class SynthUser(id:Int, shape:Double, mean:Double, 
    locations:Array[Location], prefCDF:Array[Double]) {
  val gen = Gamma(shape, mean/shape)
  val searchSrc = scala.collection.Searching.search(prefCDF)
  val rng = new scala.util.Random(id)
  var day = 0
  def draw = {
    val t = gen.draw()
    val day = (t/(24*60*60)).round // days til next event
    val loc = locations(searchSrc.search(rng.nextDouble).insertionPoint)
    val event = loc.draw
    
    LoginEvent(id, loc.id, event.startDaySecs, event.endDaySecs)
  }
}
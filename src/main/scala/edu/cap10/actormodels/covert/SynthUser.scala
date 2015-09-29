package edu.cap10.actormodels.covert

import breeze.stats.distributions._

case class LoginEvent(user_id:UserID, location_id:HotSpotID, login:Long, logout:Long)

/**
 * @author cap10
 */
case class SynthUser(id:Int,
    shape:Double, mean:Double, 
    locations:Array[Location], prefPDF:Array[Double],
    geomP:Double) {
  
  // build prefs
  
  val pdfHour = {
    val lp = locations.zip(prefPDF)
    val pref = (0 to 23).toArray.map { hour =>
      lp.map{ case(l, p) => l.pdf(hour)*p }.sum
    }
    val tot = pref.sum
    pref.map(_ / tot)
  }
  val cdfHour = pdf2CDF(pdfHour)
  
  val pdfLocs = {
    Array.tabulate[Double](24, locations.size) {
      (hour, l) => locations(l).pdf(hour)
    } map { x => {
      val tot = x.sum
      x.map(_ / tot)
    } }
  }
  
  val cdfLocs = pdfLocs.map { x => pdf2CDF(x) }
  
  val gen = Gamma(shape, mean/shape)
  val geomVisits = Geometric(geomP)
  val searchSrc = scala.collection.Searching.search(prefPDF)
  val rng = new scala.util.Random(id)

  var tilEvent : Int = gen.draw.toInt
  var meeting : Option[LoginEvent] = None
  def meet(l:Location, e:UseEvent) = {
    meeting = Some(LoginEvent(id, l.id, e.startDaySecs, e.endDaySecs))
  }
  
  val maxVisits = scala.math.min(pdfHour.filter(_ > 0).size, 5)
  
  def tick = {
    val res : List[LoginEvent] = if (!meeting.isEmpty) {
      List(meeting.get)
    } else if (tilEvent == 0) {
      val n = scala.math.min(geomVisits.draw(), 5)
      var pdfs = pdfHour.clone
      var left = 1.0
      (0 until n).map { _ =>
        var draw = rng.nextDouble() * left
        val hour = pdfFind(pdfs, draw)
        left -= pdfs(hour)
        pdfs(hour) = 0
        hour
      }.map { hour =>
        val drw = rng.nextDouble
        val insert = pdfFind(pdfLocs(hour), drw)
        val l = locations(insert)
        System.out.println(pdfLocs(hour) mkString ", ")
        System.out.println(hour)
        System.out.println(l.pdf mkString ", ")
        val event = l.draw(hour)
        LoginEvent(id, l.id, event.startDaySecs, event.endDaySecs)
      }.toList
    } else List()
    
    if (tilEvent == 0) {
      tilEvent = gen.draw.toInt
    } else tilEvent -= 1
    res
  }

}

object Main extends App {
  val locs = scala.util.Random.shuffle(Locations.alllocs.toList).take(5).toArray
  val rpdf = {
    val src = Array.fill(5)(scala.util.Random.nextDouble)
    val tot = src.sum
    src.map(_ / tot)
  }
  val synth = SynthUser(1, 1d, 10d, locs, rpdf, 0.9)
  for (i <- 0 to 100) synth.tick match {
    case List() =>
    case l => System.out.println(l)
  }
}
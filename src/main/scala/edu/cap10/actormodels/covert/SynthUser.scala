package edu.cap10.actormodels.covert

import breeze.stats.distributions._

case class LoginEvent(user_id:UserID, location_id:HotSpotID, login:Long, logout:Long, purpose:String = "background") {
  override val toString = f"$user_id,$location_id,$login,$logout,$purpose"
}

import scala.math.Ordering

object LoginEventOrder extends Ordering[LoginEvent] {
  def compare(e1:LoginEvent, e2:LoginEvent) = e1.login compare e2.login
}

/**
 * @author cap10
 */
case class SynthUser(id:Int,
    shape:Double, mean:Double, binop:Double, 
    locations:Array[Location], prefPDF:Array[Double]
) {
  
  // build prefs
  
  assert(locations.length == prefPDF.length)
  assert(Math.abs(prefPDF.sum - 1) <= 1e-6)
  
  val pdfHour = {
    val lp = locations.zip(prefPDF)
    val pref = (0 to 23).toArray.map { hour =>
      lp.map{ case(l, p) => l.pdf(hour)*p }.sum
    }
    val tot = pref.sum
    pref.map(_ / tot)
  }

  val maxVisits = pdfHour.filter(_ != 0d).length
  
  val pdfLocs = {
    Array.tabulate[Double](24, locations.size) {
      (hour, l) => locations(l).pdf(hour)
    } map { x => {
      val tot = x.sum
      x.map(_ / tot)
    } }
  }
   
  val gen = Gamma(shape, mean/shape)
  val geomVisits = Binomial(maxVisits-1, binop*9/(maxVisits-1))
  val rng = new scala.util.Random(id)

  var tilEvent : Int = gen.draw.toInt
  var meeting : Option[LoginEvent] = None
  def meet(l:Location, e:UseEvent) = {
    meeting = Some(LoginEvent(id, l.id, e.startDaySecs, e.endDaySecs, "covert"))
  }
  
  def drawHour(pdf:Double) = ???
  
  def tick(day:Int) = {
    val dayOffset : Long = day*24*60*60
    val res : List[LoginEvent] = if (!meeting.isEmpty) {
      val res = meeting.get
      meeting = None
      List(res.copy(login=res.login+dayOffset, logout=res.logout+dayOffset))
    } else if (tilEvent == 0) {
      val n = geomVisits.draw()+1
      var pdfs = pdfHour.clone
      var left = 1.0
      val hours = Array.ofDim[Int](n)
      (0 until n).map { i =>
        var draw = rng.nextDouble() * left
        val hour = pdfFind(pdfs, draw)
        left = left - pdfs(hour)
        pdfs(hour) = 0
        hours(i) = hour
      }
          
      hours.sorted.map { hour =>
        val drw = rng.nextDouble
        val insert = pdfFind(pdfLocs(hour), drw)
        val l = locations(insert)
        val event = l.draw(hour)
        LoginEvent(id, l.id, event.startDaySecs+dayOffset, event.endDaySecs+dayOffset)
      }.toList
    } else List()
    
    if (tilEvent == 0) {
      tilEvent = gen.draw.toInt
    } else tilEvent -= 1
    res
  }

}

object Main extends App {
  val userfile = args(0)
  val location = Locations.alllocs(args(1).toInt-1)
  val shape = args(2).toDouble
  val mean = args(3).toDouble
  val gen = Gamma(shape, mean/shape)
  val users = io.Source.fromFile(userfile).getLines.zipWithIndex.map { case (line, i) =>
    val (same, varying) = line.split(" ").splitAt(3)
    val Array(shape, mean, binop) = same.map(_.toDouble)
    val len = varying.length / 2
    val (locs, prefs) = varying.splitAt(len)
    SynthUser(i+1, shape, mean, binop, Locations.get(locs.map(_.toInt - 1)), prefs.map(_.toDouble))
  }.toList
  
  var timeToMeet = gen.draw.toInt
  
  for (i <- 0 to 2*365) {
    if (timeToMeet == 0) { // is meeting day?
      val List(first, second) = scala.util.Random.shuffle(users).take(2) // if yes, who meeting?
      val hr = pdfFind(location.pdf, scala.util.Random.nextDouble)
      val firstVisit, secondVisit = location.draw(hr)
      first.meet(location, firstVisit)
      second.meet(location, secondVisit)
      // give them meeting event
      timeToMeet = gen.draw.toInt
    } else timeToMeet -= 1
    users.map(_.tick(i)).flatten.sorted(LoginEventOrder).foreach { println }
//    for (synth <- users) synth.tick(i) match {
//      case List() =>
//      case l => l foreach println
//    }
  }
}
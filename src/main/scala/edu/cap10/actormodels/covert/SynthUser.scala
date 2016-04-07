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
case class SynthUser(
    id:Int,
    waitingDistro:Rand[Double],
    binop:Double,
    locations:Array[Location], prefPDF:Array[Double]
) {

  // build prefs

  assert(locations.length == prefPDF.length)
  assert(Math.abs(prefPDF.sum - 1) <= 1e-6)
  assert(locations.find({ _ == null }).isEmpty, id.toString + ":" + locations.indexOf(null) + "\n" + (locations mkString "\n"))

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
  
  //val gen = Gamma(shape, mean/shape)
  val geomVisits = Binomial(maxVisits-1, binop*9/(maxVisits-1))
  val rng = new scala.util.Random(id)

  var tilEvent : Int = waitingDistro.draw.toInt
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
      (0 until n).foreach { i =>
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
      tilEvent = waitingDistro.draw.toInt
    } else tilEvent -= 1
    res
  }

}

object SynthUser {
  def build(in: (String, Int)) : SynthUser = {
    val (same, varying) = in._1.split(" ").splitAt(3)
    val Array(shape, mean, binop) = same.map(_.toDouble)
    val len = varying.length / 2
    val (locs, prefs) = varying.splitAt(len)
    SynthUser(in._2+1, Gamma(shape, mean/shape), binop, Locations.get(locs.map(_.toInt - 1)), prefs.map(_.toDouble))
  }
}

case class RunConfig(
  users:List[SynthUser],
  covertLocation:Location,
  covertMeetingGenerator:Rand[Double],
  meetingTimePDF:Array[Double],
  durationYears:Int
)

object RunConfig {
  def build(args:Array[String]) : RunConfig = {
    val input = io.Source.fromFile(args(0)).getLines
    
    val location_id = input.next.toInt
    val covertLocation = Locations.alllocs(location_id-1)
    val users = input.zipWithIndex.map { SynthUser.build }.toList
    
    val Array(shape, mn) = args.slice(1, 3).map { _.toDouble }
    
    val durationYears = args(3).toInt
    
    val locProb = if (args.length == 4) {
      val temp = covertLocation.pdf.map { p => if (p == 0d) p else 1-p }
      val tot = temp.sum
      temp.map(_/tot)
    } else {
      covertLocation.pdf
    }
    
    RunConfig(users, covertLocation, Gamma(shape, mn/shape), locProb, durationYears)
  }
}

object Main extends App {
  val rc = RunConfig.build(args)
  import rc._

  var timeToMeet = covertMeetingGenerator.draw.toInt

  for (i <- 0 to durationYears*365) {
    if (timeToMeet == 0) { // is meeting day?
      val List(first, second) = scala.util.Random.shuffle(users).take(2) // if yes, who meeting?
      val hr = pdfFind(covertLocation.pdf, scala.util.Random.nextDouble)
      val firstVisit, secondVisit = covertLocation.draw(hr)
      first.meet(covertLocation, firstVisit)
      second.meet(covertLocation, secondVisit)
      timeToMeet = covertMeetingGenerator.draw.toInt // draw new meeting day
    } else timeToMeet -= 1
    users.map(_.tick(i)).flatten.sorted(LoginEventOrder).foreach { println }
//    for (synth <- users) synth.tick(i) match {
//      case List() =>
//      case l => l foreach println
//    }
  }
}

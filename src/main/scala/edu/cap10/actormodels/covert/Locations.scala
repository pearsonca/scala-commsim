package edu.cap10.actormodels.covert

import edu.cap10.actormodels.covert._
import scala.util.Random

import breeze.stats.distributions._

case class UseEvent(startDaySecs:Long, endDaySecs:Long)

case class Location(id: HotSpotID, cdf:Array[Double], means:Array[Double], ks:Array[Double]) {
  val rng = new scala.util.Random(id)
  val gen = means.zip(ks).map { case (mu, k) => Gamma(k, mu/k) }
  val searchSrc = scala.collection.Searching.search(cdf)
  def draw = {
    var hour = searchSrc.search(rng.nextDouble).insertionPoint
    while (hour != 0 && cdf(hour) == cdf(hour-1)) { // guarantee first insertion point
      hour = hour - 1
    }
    val mean = means(hour)
    val start = hour*3600 + rng.nextInt(3600)
    val end = start + gen(hour).draw().round
    UseEvent(start, end)
  }
  
  override val toString = {
    id.toString() + System.lineSeparator() +
    "probabilities ("+cdf.size+"): "+ cdf.mkString(", ") +System.lineSeparator() + //
    "means("+means.size+"): "+ means.mkString(", ") +System.lineSeparator() // 
  }
}

/**
 * @author cap10
 */
object Locations {
  // load locations file
  // format: two files, one probabilities, one poisson means
  // each line is a location id, followed by info for 0, 1, 2, ..., 23 hr of day
  
  val alllocs = {
    val stream_probs = io.Source.fromFile(locationProbSrc).getLines.toList
    val stream_means = io.Source.fromFile(locationMeanSrc).getLines.toList
    val stream_ks = io.Source.fromFile(locationShapeSrc).getLines.toList
    
    val stream = (stream_probs, stream_means, stream_ks).zipped.toList
    
    stream.map { case (pline, mline, kline) =>
      val means = mline.split(",").tail.map(_.trim).map(_.toDouble)
      val ks    = kline.split(",").tail.map(_.trim).map(_.toDouble)
      val other    = pline.split(",").map(_.trim)
      
      val ps  = other.tail.map(_.toDouble)
      val cdf = ps.tail.scan(ps.head)( (a, b) => a+b )
      var fromhour = 23
      while(cdf(fromhour) == cdf(fromhour-1)) fromhour = fromhour - 1
      for (i <- fromhour to 23) cdf(i) = 1.0
      Location(ps.head.toInt, cdf, means, ks)
    }.toArray
  }

}

object Main extends App {
  for (l <- Locations.alllocs) System.out.println(l)
}
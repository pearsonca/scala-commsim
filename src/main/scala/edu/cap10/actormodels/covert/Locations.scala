package edu.cap10.actormodels.covert

import edu.cap10.actormodels.covert._
import scala.util.Random

import breeze.stats.distributions._

import util.Properties.lineSeparator

case class UseEvent(startDaySecs:Long, endDaySecs:Long)

case class Location(id: HotSpotID, val pdf:Array[Double], means:Array[Double], ks:Array[Double]) {
  val rng = new scala.util.Random(id)
  val gen = means.zip(ks).map { 
    case (mu, k) if k != 0d => Gamma(k, mu/k)
    case _ => null
  }
   
  def draw(hour:Int) = {
    assert(gen(hour) != null, hour)
    val mean = means(hour)
    val start = hour*3600 + rng.nextInt(3600)
    val end = start + gen(hour).draw().round
    UseEvent(start, end)
  }
  
  override val toString = {
    id.toString() + lineSeparator +
    "probabilities ("+pdf.size+"): "+ pdf.mkString(", ") + lineSeparator + //
    "means("+means.size+"): "+ means.mkString(", ") + lineSeparator // 
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
    val stream_pdf = io.Source.fromFile(locationPDFSrc).getLines.toList
    val stream_means = io.Source.fromFile(locationMeanSrc).getLines.toList
    val stream_ks = io.Source.fromFile(locationShapeSrc).getLines.toList
    
    val stream = (stream_pdf, stream_means, stream_ks).zipped.toList
    
    val prelim = stream.map { case (pline, mline, kline) =>
      val means = strsToDoubles(mline.split(",").tail)
      val ks    = strsToDoubles(kline.split(",").tail)
      val other = pline.split(",")
      val cdf   = strsToDoubles(other.tail)
      Location(other.head.trim.toInt, cdf, means, ks)
    }.toArray
    
    val maxid = prelim.map(_.id.toInt).max + 1
    val res = Array.ofDim[Location](maxid)
    prelim.foreach { l => res(l.id.toInt) = l }
    res
  }

  def get(l:Iterable[Int]) = l.map(alllocs(_)).toArray
  
}

//object Main extends App {
//  for (l <- Locations.alllocs) System.out.println(l)
//}

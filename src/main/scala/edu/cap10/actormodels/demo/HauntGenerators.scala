package edu.cap10.actormodels.demo

import scala.util.Random
import edu.cap10.util.NaturalInt
import scala.languageFeature.implicitConversions

case class HauntGenerator(rng : Random, locations : Seq[LocationID]) {

  require(locations.size > 0, "Cannot sample from non-existent locations.")
  private val backend = locations.toArray
  lazy val pdf = Array.fill(backend.length)(1d/backend.length)
  lazy val cdf = pdf.scan(0d)(_ + _).drop(1) // TODO replace w/ Kahan Summation
  import scala.collection.Searching._
  
  private def select(count:Int, which: =>Int) = Seq.fill(count)(which) map backend
  
  lazy val uniform : (Int) => Seq[LocationID] = {
    (count : Int) => {
      require(count >= 0, "Cannot request a negative location count.")
      require(count <= locations.size, "Asked for more locations than exist.")
      select(count, rng.nextInt(backend.length)) 
//        p => cdf.search(p) match {
//          case Found(index) => backend(index+1)
//          case InsertionPoint(index) => backend(index)
//        }
//      }
//      rng.shuffle(locations).take(count)
    } ensuring(_.size == count)
  }
 
  def distributed(locations: Seq[LocationID], min : Int = 1) : (Double) => Seq[LocationID] = {
    require(locations.size > 0, "Cannot sample from non-existent locations.")
    (mean : Double) => {
      require(mean >= min, "Cannot request a mean number of locations less than the min "+min)
      require(mean < locations.size, "The expected number of locations can be the total number of locations.")
      val p = (mean - min) / (locations.size - min)
      val (keep, possible) = rng.shuffle(locations).splitAt(min)
      keep ++ possible.filter { _ => rng.nextDouble < p }
    } ensuring(_.size >= min)
  }
  
  def weighted(locations: Seq[LocationID], weights: Seq[Double]) : 
  
}
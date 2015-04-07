package edu.cap10.actormodels.demo

import edu.cap10.util.Probability
import edu.cap10.util.Probability._
import scala.language.implicitConversions

case class DataParams(
  uniqueLocations:Int,
  totalDays:Int,
  dailyVisitProb : Probability,
  meanVisitDuration : Double, // seconds
  meanVisitedLocations : Double
)

object MontrealProps extends DataParams(345, 356*5, 0.5, 5*60, 4)

class LocationParams

class AdvancedDataParams
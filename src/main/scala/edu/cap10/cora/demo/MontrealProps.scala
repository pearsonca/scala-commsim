package edu.cap10.cora.demo
import edu.cap10.util.Probability
import edu.cap10.util.Probability._
import scala.language.implicitConversions

case class ReferenceConfig(uniqueLocs:Int, totalDays:Int, dailyVisitProb : Probability, averageVisitDuration:Double, avgLocs:Double)

object MontrealProps extends ReferenceConfig(345, 356*5, 0.5, 1, 4)
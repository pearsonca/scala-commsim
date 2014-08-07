package edu.cap10

import scala.util.Random.nextInt
import scala.language.implicitConversions

package object util {

  implicit def hour2RandomTimeStamp(hour:Int) = TimeStamp(hour, nextInt(60), nextInt(60))
  
}
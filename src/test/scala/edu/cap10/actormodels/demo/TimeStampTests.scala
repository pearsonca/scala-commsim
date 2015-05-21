package edu.cap10.actormodels.demo

import org.scalatest.FunSuite

import edu.cap10.util.TimeStamp
import edu.cap10.util.TimeStamp._

class TimeStampTests extends FunSuite {
 
  // TODO get scalacheck working 
  
  test("timestamp hours must be between 0 and 23") {
    intercept[IllegalArgumentException] { Hour(-1) }
    intercept[IllegalArgumentException] { Hour(24) }
  }
  
  test("timestamp minutes must be between 0 and 59") {
    intercept[IllegalArgumentException] { Minute(-1) }
    intercept[IllegalArgumentException] { Minute(60) }
  }
  
  test("timestamp seconds must be between 0 and 59") {
    intercept[IllegalArgumentException] { Second(-1) }
    intercept[IllegalArgumentException] { Second(60) }
  }
  
}
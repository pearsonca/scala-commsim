package edu.cap10.actormodels.covert

import org.scalatest.FunSuite
import org.scalactic.Tolerance._

/**
 * @author cap10
 */
class SynthUserTests extends FunSuite {
  
  val thing = Array.fill(12)(0d)
  
  
  val testLocOne = Location(-1, Array.fill(24)(1d/24), Array.fill(24)(1d), Array.fill(24)(1d))
  val testLocTwo = Location(-2,
      (List.fill(12)(0d) ++ List.fill(12)(1d/12)).toArray,
      Array.fill(24)(1d),
      (List.fill(12)(0d) ++ List.fill(12)(1d)).toArray
  )
  
  test("a synth user with one location just uses that location pdf") {
    val userA = SynthUser(-1, 1, 1, Array(testLocOne), Array(1.0), .9)
    assert(userA.pdfHour.length === testLocOne.pdf.length)
    for (i <- 0 until 24) assert(userA.pdfHour(i) === testLocOne.pdf(i) +- 1e-8)
  }
}
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
    val userA = SynthUser(-1, 1, 1, .9, Array(testLocOne), Array(1.0))
    assert(userA.pdfHour.length === testLocOne.pdf.length)
    for (i <- 0 until 24) assert(userA.pdfHour(i) === testLocOne.pdf(i) +- 1e-8)
  }
  
  test("a synth user with two locations uses the combined pdf, weighted by pref") {
    val userA = SynthUser(-1, 1, 1, .9, Array(testLocOne, testLocTwo), Array(0.5, 0.5))
    val ref = (List.fill(12)(1d/48) ++ List.fill(12)(3d/48)).toArray
    for (i <- 0 until 24) assert(userA.pdfHour(i) === ref(i) +- 1e-8)
    val userB = SynthUser(-1, 1, 1, .9, Array(testLocOne, testLocTwo), Array(1d/4, 3d/4))
    val refB = (List.fill(12)(1d/96) ++ List.fill(12)(7d/96)).toArray
    for (i <- 0 until 24) assert(userB.pdfHour(i) === refB(i) +- 1e-8, "problem in i = "+i)
  }
  
  test("all hours have location pdfs that sum to 1") {
    val userB = SynthUser(-1, 1, 1, .9, Array(testLocOne, testLocTwo), Array(1d/4, 3d/4))
    val refB = (List.fill(12)(1d/96) ++ List.fill(12)(7d/96)).toArray
    for (i <- 0 until 24) assert(userB.pdfLocs(i).sum === 1d +- 1e-8, "problem in i = "+i)
  }
  
  test("draws work properly") {
    val ref = (List.fill(12)(1d/48) ++ List.fill(12)(3d/48)).toArray
    val cref = ref.clone
    val drawOne = 1d/49
    assert(pdfFind(cref, drawOne) === 0)
    val left = 1 - cref(0)
    cref(0) = 0d
    assert(cref(0) !== ref(0))
    assert(pdfFind(cref, drawOne*left) === 1)
  }
  
}
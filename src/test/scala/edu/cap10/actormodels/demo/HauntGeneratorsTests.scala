package edu.cap10.actormodels.demo

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

import scala.util.Random
import scala.languageFeature.implicitConversions

class HauntGeneratorsTests extends FunSuite with BeforeAndAfter {

  val seed : Long = 10
  var rng : Seq[Random] = _
  val locations : IndexedSeq[Int] = 0 until 100
  var HG : Seq[HauntGenerator] = _
  
  before {
    rng = List(seed, seed, seed*2) map { new Random(_) }
    HG = rng map { HauntGenerator(_) }
  }
  
  test("properly set up random number generators") {
    assert(rng(0) !== rng(1))
    assert(rng(0) !== rng(2))
    assert(rng(1) !== rng(2))
    val results = rng map { _.nextDouble }
    assert(results(0) === results(1))
    assert(results(0) !== results(2))
  }
  
  test("uniform generators return the requested number of locations") {
    val gen = HG(0).uniform(locations)
    (1 to 10) map { i => assert(gen(i).size === i) }
  }
  
  test("uniform generators return different series on subsequent draws") {
    val gen = HG(0).uniform(locations)
    assert(gen(5) !== gen(5))
  }
  
  test("uniform generators with same rng series return same location sets") {
    val gen1 = HG(0).uniform(locations)
    val gen2 = HG(1).uniform(locations)
    assert(gen1(5) === gen2(5))
    assert(gen1(5) === gen2(5))
  }
  
  test("uniform generators with different rng series return different location sets") {
    val gen1 = HG(0).uniform(locations)
    val gen2 = HG(2).uniform(locations)
    assert(gen1(5) !== gen2(5))
    assert(gen1(5) !== gen2(5))
  }
  
  test("uniform generators complain when initialized with empty location lists") {
    intercept[IllegalArgumentException] {
      HG(0).uniform(Seq.empty)
    }
  }
  
  test("uniform generators complain when asked for too many elements") {
    intercept[IllegalArgumentException] {
      HG(0).uniform(Seq(1))(2)
    }
  }
  
}
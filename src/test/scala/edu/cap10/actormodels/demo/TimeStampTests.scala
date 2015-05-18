package edu.cap10.actormodels.demo

import org.scalatest.{
  FunSuite,
  BeforeAndAfter
}
import org.scalatest.prop.PropertyChecks

import scala.util.Random
import scala.language.implicitConversions

import edu.cap10.util.TimeStamp
import edu.cap10.util.TimeStamp._

class TimeStampTests extends FunSuite with BeforeAndAfter with PropertyChecks {
 
  val seed : Long = 10
  var rng : Seq[Random] = _
  
  test("TimeStamp Hour should be between 0 and 23") { 
    forAll { (n:Int) => 
      whenever(n < 0 || n > 23) {
        intercept[IllegalArgumentException] { Hour(n) }
      }
    }
  }
  
  test("TimeStamp Minute should be between 0 and 59") { 
    forAll { (n:Int) => 
      whenever(n < 0 || n > 59) {
        intercept[IllegalArgumentException] { Minute(n) }
      }
    }
  }
  
  test("TimeStamp Second should be between 0 and 59") { 
    forAll { (n:Int) => 
      whenever(n < 0 || n > 59) {
        intercept[IllegalArgumentException] { Second(n) }
      }
    }
  }
//  val locations : IndexedSeq[Int] = 0 until 100
//  var HG : Seq[HauntGenerator] = _
//  
//  before {
//    rng = List(seed, seed, seed*2) map { new Random(_) }
//    HG = rng map { HauntGenerator(_, locations) }
//  }
//  
//  test("properly set up random number generators") {
//    assert(rng(0) !== rng(1), "rng(0) vs rng(1) refs are incorrectly equal.")
//    assert(rng(0) !== rng(2), "rng(0) vs rng(2) refs are incorrectly equal.")
//    assert(rng(1) !== rng(2), "rng(1) vs rng(2) refs are incorrectly equal.")
//    val results = rng map { _.nextDouble }
//    assert(results(0) === results(1))
//    assert(results(0) !== results(2))
//  }
//  
//  test("uniform generators return the requested number of locations") {
//    val gen = HG(0).uniform
//    (1 to 10) map { i => assert(gen(i).size === i) }
//  }
//  
//  test("uniform generators return different series on subsequent draws") {
//    val gen = HG(0).uniform
//    assert(gen(5) !== gen(5))
//  }
//  
//  test("uniform generators with same rng series return same location sets") {
//    val gen1 = HG(0).uniform
//    val gen2 = HG(1).uniform
//    assert(gen1(5) === gen2(5))
//    assert(gen1(5) === gen2(5))
//  }
//  
//  test("uniform generators with different rng series return different location sets") {
//    val gen1 = HG(0).uniform
//    val gen2 = HG(2).uniform
//    assert(gen1(5) !== gen2(5))
//    assert(gen1(5) !== gen2(5))
//  }
//  
//  test("uniform generators complain when initialized with empty location lists") {
//    intercept[IllegalArgumentException] {
//      HauntGenerator(new Random, Seq.empty)
//    }
//  }
//  
//  test("uniform generators complain when asked for too many elements") {
//    intercept[IllegalArgumentException] {
//      HG(0).uniform(locations.size+1)
//    }
//  }
  
}
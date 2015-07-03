package edu.cap10.actormodels.covert

import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.util.Random
import scala.language.implicitConversions

class HotSpotTests extends FunSuite with BeforeAndAfter {
    
  test("sending ticks to a HotSpot advances its clock") {
    val HS1 = HotSpot(1, "log", 5, 10)
    assert(HS1.clock === 0)
    for (i <- 1 to 5) {
      HS1.tick(i)
      assert(HS1.clock === i)
    }
  }

  test("HotSpot tick returns NotYetActive until time it turns on") {
    val turnon = 10
    val HS1 = HotSpot(1, "log", turnon, turnon + 10)
    (1 until turnon).foreach( i => {
      assert(HS1.tick(i) === NotYetActive, s"tick at $i not fine") 
    })
    assert(HS1.tick(turnon) !== NotYetActive, s"tick at $turnon not fine")
  }
  
  test("HotSpot tick returns Active after it turns on, until it turns off") {
    val (turnon, turnoff) = (10, 20)
    val HS1 = HotSpot(1, "log", turnon, turnoff)
    for (i <- 1 until turnon) HS1.tick(i)
    for (j <- turnon until turnoff) assert(HS1.tick(j) === Active)
    assert(HS1.tick(turnoff) !== Active)
  }
  
}
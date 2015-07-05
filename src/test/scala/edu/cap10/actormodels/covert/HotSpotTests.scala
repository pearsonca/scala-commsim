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
  
  test("HotSpot tick returns NoLongerActive after it turnsoff") {
    val (turnon, turnoff) = (10, 20)
    val HS1 = HotSpot(1, "log", turnon, turnoff)
    for (i <- 1 until turnoff) HS1.tick(i)
    assert(HS1.tick(turnoff) === NoLongerActive)
    assert(HS1.tick(turnoff+1) === NoLongerActive)
  }
  
  test("when not active, logging in returns false") {
    val HS1 = HotSpot(1, "log", 10, 10)
    assert(HS1.login(1, 0) === false)
    assert(HS1.login(2, 0) === false)
  }
  
  test("when active, logging in with a new user returns true") {
    val HS1 = HotSpot(1, "log", 0, 10)
    assert(HS1.login(1, 0) === true)
    assert(HS1.login(2, 0) === true)
  }
  
  test("when active, logging in with a user that hasn't logged out returns false") {
    val HS1 = HotSpot(1, "log", 0, 10)
    assert(HS1.login(1, 0) === true)
    assert(HS1.login(1, 0) === false)
  }
  
  test("when active, logging out with a user resets their ability to login") {
    val HS1 = HotSpot(1, "log", 0, 10)
    assert(HS1.login(1, 0) === true)
    HS1.logout(1, 1)
    assert(HS1.login(1, 2) === true)    
  }
  
}
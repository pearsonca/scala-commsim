package edu.cap10.actormodels.covert

import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.util.Random
import scala.language.implicitConversions

class ProvocateurTests extends FunSuite {
    
  test("sending ticks to a Provo advances its clock") {
    val P1 = Provocateur(1, 1)
    assert(P1.clock === 0)
    for (i <- 1 to 5) {
      P1.tick(i)
      assert(P1.clock === i)
    }
  }
  
  test("Provos can add HotSpots to their use list") {
    val pID = 1
    val P1 = Provocateur(pID, 1)
    val HS1 = HotSpot(1, 0, 10)
    val HS2 = HotSpot(2, 0, 10)
    assert(P1.aware(HS1), "P1 did not accept HS1, despite a 100% probability")
    assert(P1.aware(HS2), "P1 did not accept HS2, despite a 100% probability")
    assert(P1.hotspots.contains(HS1), "after accepting HS1, it didn't appear in its list")
    assert(P1.hotspots.contains(HS2), "after accepting HS2, it didn't appear in its list")
  }
  
  test("Provos") {
    
  }
  
}
package edu.cap10.actormodels.covert

import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.util.Random
import scala.language.implicitConversions

class HotSpotTests extends FunSuite with BeforeAndAfter {

  var HS1 : HotSpot = _
  
  before {
    HS1 = HotSpot(1, "log", 5, 10)
  }
  
  test("sending ticks to a HotSpot advances its clock") {
    assert(HS1.clock === 0)
    for (i <- 1 to 5) {
      HS1.tick(i)
      assert(HS1.clock === i)
    }
  }

  
}
package edu.cap10.actormodels.covert

import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.util.Random
import scala.language.implicitConversions

class ProvocateurTests extends FunSuite {
    
  test("sending ticks to a HotSpot advances its clock") {
    val P1 = Provocateur(1)
    assert(P1.clock === 0)
    for (i <- 1 to 5) {
      P1.tick(i)
      assert(P1.clock === i)
    }
  }
  
}
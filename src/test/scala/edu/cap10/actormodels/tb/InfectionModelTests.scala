package edu.cap10.actormodels.tb

import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.util.Random
import scala.language.implicitConversions

import TBStrain.{values => strains, _}

import edu.cap10.util.Probability._

class InfectionModelTests extends FunSuite with BeforeAndAfter {

  val nonSusceptibleHosts = Seq() ++ (
    strains map Resistant
  ) ++ (
    strains map Exposed
  ) ++ (
    strains map Infectious
  ) ++ (
    strains map Treated
  )
  
  val allHostStates : Seq[HostTBState] = Seq(Susceptible) ++
    nonSusceptibleHosts ++ (
    strains map Chronic
  )
  
  val seed : Long = 1
  
  val nullModel = ModelParams(1.0, 1)
  val refModel = ModelParams(0.3, 2)
  val lowModel = ModelParams(0.1, 1)
  
  var rng : Random = _
  
  before {
    rng = new Random(seed)
  }
  
  test("exposing hosts to no TB returns Option == None") {
    val infModel = InfectionModel(nullModel, seed)
    val res = allHostStates map { hs => infModel(hs, Seq.empty) }
    assert(res.forall { _.isEmpty } === true)
  }
  
  test("exposing hosts that are Exposed, Resistant, or Infectious (with any strain) returns Option == None") {
    val infModel = InfectionModel(nullModel, seed)
    val res = nonSusceptibleHosts map { hs => infModel(hs, strains.toSeq) }
    assert(res.forall { _.isEmpty } === true)
  }
  
  test("exposing Susceptible hosts to one infectious contact of a particular strain, returns an Exposed of the appropriate type, when the infection probability is 1.0") {
    val infModel = InfectionModel(nullModel, seed)
    strains.foreach { strain =>
      assert(infModel(Susceptible, Seq(strain)) === Option(Exposed(strain))) 
    }
  }

  test("exposing Susceptible hosts to one infectious contact of a particular strain, returns an Exposed of the appropriate type, at the appropriate rate, when the infection probability is not 1.0") {
    val infModel = InfectionModel(refModel, seed) 
    strains foreach { strain =>
      val draws : Seq[Option[HostTBState]] = Seq.fill(10)( rng.nextDouble() < refModel.infection ) map { if(_) Some(Exposed(strain)) else None }
      val res = Seq.fill(10)( infModel(Susceptible, Seq(strain)) )
      assert(res === draws)
    } 
  }
  
  test("exposing Chronic (of either type) hosts to one infectious contact of a particular strain, returns an Infected of the appropriate type, at the appropriate rate, when the infection probability is not 1.0") {
    val infModel = InfectionModel(refModel, seed)
    import refModel._
    strains foreach { strain =>
      val draws : Seq[Option[HostTBState]] = Seq.fill(10)( rng.nextDouble() < (1-Math.pow(infection.complement, chronicEnhancement)) ) map { if(_) Some(Infectious(strain)) else None }
      val res = Seq.fill(10)( infModel(Chronic(TBStrain.Resistant), Seq(strain)) )
      assert(res === draws)
    } 
  }
  
  test("exposure to multiple infectious contacts makes for higher prob of infection") {
    val infModel = InfectionModel(lowModel, seed)
    import lowModel._
    strains foreach { strain =>
      (1 to 100).foreach { _ =>
        val notinfect = Stream.fill(5)(
            rng.nextDouble() < (1-Math.pow(infection.complement, chronicEnhancement))
        ).forall( b => !b )
        assert(infModel(Susceptible, Seq.fill(5)(strain)) === (if (notinfect) None else Some(Exposed(strain))))
      }
    }
  }
  
}
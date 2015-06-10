package edu.cap10.actormodels.tb

import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.util.Random

import TBStrain.{values => strains, _}

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
  
  var rng : Random = _
  
  before {
    rng = new Random(seed)
  }
  
  test("exposing hosts to no TB returns Option == None") {
    val infModel = InfectionModel(0.5, seed)
    val res = allHostStates map { hs => infModel(hs, Seq.empty) }
    assert(res.forall { _.isEmpty } === true)
  }
  
  test("exposing hosts that are Exposed, Resistant, or Infectious (with any strain) returns Option == None") {
    val infModel = InfectionModel(0.5, seed)
    val res = nonSusceptibleHosts map { hs => infModel(hs, strains.toSeq) }
    assert(res.forall { _.isEmpty } === true)
  }
  
  test("exposing Susceptible hosts to one infectious contact of a particular strain, returns an Exposed of the appropriate type, when the infection probability is 1.0") {
    val infModel = InfectionModel(1.0, seed)
    strains.foreach { strain =>
      assert(infModel(Susceptible, Seq(strain)) === Option(Exposed(strain))) 
    }
  }

  test("exposing Susceptible hosts to one infectious contact of a particular strain, returns an Exposed of the appropriate type, at the appropriate rate, when the infection probability is not 1.0") {
    val infProb = 0.3
    val infModel = InfectionModel(infProb, seed) 
    strains foreach { strain =>
      val draws : Seq[Option[HostTBState]] = Seq.fill(10)( rng.nextDouble() < infProb ) map { if(_) Some(Exposed(strain)) else None }
      val res = Seq.fill(10)( infModel(Susceptible, Seq(strain)) )
      assert(res === draws)
    } 
  }
  
}
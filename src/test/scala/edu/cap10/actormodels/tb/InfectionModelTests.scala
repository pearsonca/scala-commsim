package edu.cap10.actormodels.tb

import org.scalatest.FunSuite

import TBStrain.{values => strains, _}

class InfectionModelTests extends FunSuite {

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
  
  test("exposing hosts to no TB returns Option == None") {
    val infModel = InfectionModel(0.5)
    val res = allHostStates map { hs => infModel(hs, Seq.empty) }
    assert(res.forall { _.isEmpty } === true)
  }
  
  test("exposing hosts that are Exposed, Resistant, or Infectious (with any strain) returns Option == None") {
    val infModel = InfectionModel(0.5)
    val res = nonSusceptibleHosts map { hs => infModel(hs, strains.toSeq) }
    assert(res.forall { _.isEmpty } === true)
  }
  
  test("exposing Susceptible hosts to one infectious contact of a particular strain, returns an Exposed of the appropriate type, when the infection probability is 1.0") {
    val infModel = InfectionModel(1.0)
    strains.foreach { strain =>
      assert(infModel(Susceptible, Seq(strain)) === Option(Exposed(strain))) 
    }
  }

}
package edu.cap10.actormodels.tb

import org.scalatest.FunSuite

import TBStrain.{values => strains, _}

class InfectionModelTests extends FunSuite {

  val allHostStates : Seq[HostTBState] = Seq(Susceptible) ++ (
    strains map Resistant
  ) ++ (
    strains map Exposed
  ) ++ (
    strains map Infectious
  ) ++ (
    strains map Treated
  ) ++ (
    strains map Chronic
  )

  test("exposing hosts to no TB returns Option == None") {
    val infModel = InfectionModel(0.5)
    val res = allHostStates map { hs => infModel(hs, Seq.empty) }
    assert(res.forall { _.isEmpty } === true)
  }

}
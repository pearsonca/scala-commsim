package edu.cap10.actormodels.tb

import TBStrain._

case class InfectionModel(some:Double)
  extends Function2[HostTBState, Seq[TBStrain], Option[HostTBState]]
{
  def apply(
    hostTBstate : HostTBState,
    infectiousContacts:Seq[TBStrain]
  ) : Option[HostTBState] = ???
}
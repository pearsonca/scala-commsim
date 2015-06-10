package edu.cap10.actormodels.tb

import TBStrain._
import edu.cap10.util.LocalRNG


case class InfectionModel(infectionProb:Double, val seed:Long)
  extends Function2[HostTBState, Seq[TBStrain], Option[HostTBState]]
  with LocalRNG
{
  def apply(
    hostTBstate : HostTBState,
    infectiousContacts:Seq[TBStrain]
  ) : Option[HostTBState] = infectiousContacts match {
    case Seq() => None
    case _ => hostTBstate match {
      case Susceptible => if (rng.nextDouble() < infectionProb) Some(Exposed(infectiousContacts.head)) else None
      case _ => None
    }
  }
}
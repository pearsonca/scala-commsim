package edu.cap10.actormodels.tb

import TBStrain.Value
import edu.cap10.util.LocalRNG

case class InfectionModel(mp:ModelParams, val seed:Long)
  extends Function2[HostTBState, Seq[TBStrain.Value], Option[HostTBState]]
  with LocalRNG
{
  import mp._
  def apply(
    hostTBstate : HostTBState,
    infectiousContacts:Seq[TBStrain.Value]
  ) : Option[HostTBState] = infectiousContacts match {
    case Seq() => None
    case _ => hostTBstate match {
      case Susceptible =>
        if (rng.nextDouble() < infection)
          Some(Exposed(infectiousContacts.head))
        else
          None
      case Chronic(_) => 
        if (rng.nextDouble() < (1-Math.pow(infection.complement, chronicEnhancement)))
          Some(Infectious(infectiousContacts.head))
        else
          None
      case _ => None
    }
  }
}
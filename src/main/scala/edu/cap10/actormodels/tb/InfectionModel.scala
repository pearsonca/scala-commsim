package edu.cap10.actormodels.tb

import TBStrain.Value
import edu.cap10.util.LocalRNG
import edu.cap10.util.Probability

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
        anyInfection(infectiousContacts, infection) map { strain => Exposed(strain) }
      case Chronic(_) =>
        anyInfection(infectiousContacts, (1-Math.pow(infection.complement, chronicEnhancement))).map { strain => Infectious(strain) }
      case _ => None
    }
  }
  
  def anyInfection(contacts:Seq[TBStrain.Value], success:Probability) : Option[TBStrain.Value] = 
    contacts.find(_ => rng.nextDouble() < success)
  
}
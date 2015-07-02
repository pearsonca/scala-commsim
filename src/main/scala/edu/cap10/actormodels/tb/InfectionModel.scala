package edu.cap10.actormodels.tb

import TBStrain.{Value => Strain}
import edu.cap10.util.LocalRNG
import edu.cap10.util.Probability
import edu.cap10.util.Probability._
import scala.language.implicitConversions

case class InfectionModel(mp:ModelParams, val seed:Long)
  extends Function2[HostTBState, Seq[Strain], Option[HostTBState]]
  with LocalRNG
{
  import mp._
  def apply(
    hostTBstate : HostTBState,
    infectiousContacts:Seq[Strain]
  ) : Option[HostTBState] = if (infectiousContacts.isEmpty)
    None
  else
    hostTBstate match {
      case Susceptible =>
        anyInfection(infectiousContacts, infection) map { strain => Exposed(strain) }
      case Chronic(_) =>
        anyInfection(infectiousContacts, (1-Math.pow(infection.complement, chronicEnhancement))) map { strain => Infectious(strain) }
      case Resistant(_) | Exposed(_) | Infectious(_) | Treated(_) => None
    }
  
  def anyInfection(contacts:Seq[TBStrain.Value], success:Probability) : Option[Strain] = 
    contacts.find(_ => rng.nextDouble() < success)
  
}
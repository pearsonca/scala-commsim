sealed class TBStrain
case object Resistant extends TBStrain
case object Sensitive extends TBStrain

sealed class HostTBState
case object Susceptible extends HostTBState
case class Resistant(strain:TBStrain) extends HostTBState
case class Exposed(strain:TBStrain) extends HostTBState
case class Infectious(strain:TBStrain) extends HostTBState
case class Treated(strain:TBStrain) extends HostTBState
case class Chronic(strain:TBStrain) extends HostTBState

sealed class HostHIVState
case object Negative extends HostHIVState
case class Positive(indicator:Double) extends HostHIVState
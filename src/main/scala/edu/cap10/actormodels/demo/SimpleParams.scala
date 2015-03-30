package edu.cap10.actormodels.demo

import edu.cap10.util.NaturalInt

case class SimpleParams (
  agentCount : NaturalInt,
  locationCount : NaturalInt,
  meanMeetingFrequency : Double,
  seed:Long
) {
  def parse(args:List[String]) : Option[SimpleParams] = args match {
    case Nil => Some(this)
    case "-n"::n :: rest => copy(agentCount = n.toInt).parse(rest)
    case "-l"::l :: rest => copy(locationCount = l.toInt).parse(rest)
    case "-t"::t :: rest => copy(meanMeetingFrequency = t.toDouble).parse(rest)
    case "-s"::s :: rest => copy(seed = s.toLong).parse(rest)
    case other => None
  }
}
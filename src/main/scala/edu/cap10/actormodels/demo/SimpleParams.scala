package edu.cap10.actormodels.demo

import edu.cap10.util.NaturalInt

object SimpleParams {
    val usage = """
Usage: pub [-n int] [-l int] [-t num] [-d num]
   -n: the number of agents
   -l: the number of covert meeting locations
   -t: inter-plot meeting period (days)
   -s: the random seed
"""
}

case class SimpleParams (
  agentCount : NaturalInt = 2,
  locationCount : NaturalInt = 1,
  meanMeetingFrequency : Double = 1d/10d,
  seed:Long = 0
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
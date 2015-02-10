package edu.cap10.cora.demo

case class SimConfig(agentN:Int = 10, meetLocations:Int = 2, plotPeriod : Int = 20) {
  def parse(args:List[String]) : Option[SimConfig] = args match {
    case Nil => Some(this)
    case "-n"::n::rest => this.copy(agentN=n.toInt).parse(rest)
    case "-l"::l::rest => this.copy(meetLocations=l.toInt).parse(rest)
    case "-t"::t::rest => this.copy(plotPeriod=t.toInt).parse(rest)
    case other => None
  }
}

object Pub extends App {

val usage = """Usage: pub [-n num] [-l num]
 -n: the number of agents
 -l: the number of locations"""

SimConfig().parse(args.toList) match {
  case Some(config) => println(config)
  case None => println(usage)
}

}
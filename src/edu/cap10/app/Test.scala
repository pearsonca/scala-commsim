package edu.cap10.app

import edu.cap10.person._
import edu.cap10.graph._
import java.io._

object Test {
	def main(args: Array[String]) {
	   val popSize = 1000
	   val factory = BackgroundFactory(0.2,0.1)
	   val people = factory.src.take(popSize)
	   val cliquer = CliqueUp(commType=Community.Family)
	   val pw = new PrintWriter("./commsim-test.txt")
	   iGraphELWriter.write(pw, cliquer.apply(people.iterator, popSize))
	   pw.close
	}
}
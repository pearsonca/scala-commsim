package edu.cap10.app

import edu.cap10.person._
import edu.cap10.graph._
import edu.cap10.distributions._

import java.io._

object Test {
	def main(args: Array[String]) {
	   val popSize = 1000
	   val cliqueSize = 3
	   val (pBadBack, pBadFore, pBadNormDiscount, pComm) = (0.1, 0.1, 0.5, 0.2)
	   val factory = BackgroundFactory(pComm, pBadBack)
	   val people = factory.src.take(popSize)
	   val cliquer = CliqueUp(cliqueSize,Community.Family)
	   val H = Hub(pBadBack,pBadFore*pBadNormDiscount,pComm, popSize)
	   val hMeanK = 30
	   val hConP = (hMeanK * cliqueSize).toDouble / popSize
	   val triads = CliqueAll.grouped(people.iterator,popSize, cliqueSize, Community.Family)
	   for (c <- triads if DoubleSrc.next < hConP) c.random.join(H, Community.Family)
	   
	   val clusterCount = 3
	   val clusterSize = 5
	   val terrorFactory = PlotterFactory(pComm, pBadBack, popSize+1)
	   val terrorists = terrorFactory.src.take(clusterCount * clusterSize);
	   CliqueAll.grouped(terrorists.iterator, terrorists.size, clusterSize, Community.Plot) flatten
	   val pwEL = new PrintWriter("./commsim-test.txt")
	   val output = cliquer.apply(triads) ++ CliqueAll.grouped(terrorists.iterator, terrorists.size, clusterSize, Community.Plot).flatten :+ H
	   iGraphELWriter.write(pwEL, output)
	   pwEL.close
	   val pwVI = new PrintWriter("./commsim-test-vertex-info.txt")
	   iGraphVIWriter.write(pwVI, output)
	   pwVI.close
	}
}
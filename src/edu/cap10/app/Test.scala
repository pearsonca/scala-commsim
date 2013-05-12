package edu.cap10.app

import edu.cap10.person._
import edu.cap10.graph._
import edu.cap10.distributions._

import java.io._

object Test {
	def main(args: Array[String]) {
	   val popSize = 3
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
	   
	   val (clusterCount,clusterSize) = (3,5)

	   val terrorists = PlotClusters(popSize+1, pComm, pBadBack, clusterSize).take(clusterCount)

	   H.contacts(Community.Plot) ++= Clique(Community.Plot)(terrorists)
	   val output = cliquer.apply(triads) ++ terrorists :+ H
//	   val (pwEL, pwVI) = (new PrintWriter("./commsim-test.txt"), new PrintWriter("./commsim-test-vertex-info.txt"))
//	   iGraphELWriter.write(pwEL, output).close 
//	   iGraphVIWriter.write(pwVI, output).close
	   output foreach( _.start )
	   output foreach( a => a ! "TEST")
	}
}
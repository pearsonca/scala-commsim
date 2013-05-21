package edu.cap10.app

import edu.cap10.person._
import edu.cap10.graph._
import edu.cap10.distributions._

import edu.cap10.person.Vocabulary._ // Good, Bad
import edu.cap10.person.Community._ // 

import scala.collection.mutable.Buffer

import java.io._

object Test {
	def main(args: Array[String]) = {
	  val (iterlim, simlim, popSize) = (1,1,500)
	  val (pBadBack, pBadFore, pBadNormDiscount, pComm, pForeCommDiscount, pBlend) = (0.01, 0.05, 0.5, 0.02, 0.5, 0.1)
	  val cliqueSize = 3

	   val cliquer = CliqueUp(cliqueSize,Community.Family)
	   val hMeanK = 10
	   val hConP = (hMeanK * cliqueSize).toDouble / popSize
	   val (clusterCount,clusterSize) = (3,5)
	   val (test,done) = ( SimulationCommand(SimulationEvent.TEST, 0), SimulationCommand(SimulationEvent.DONE, 0) )
	   val (updater,nexter) = (SimulationCommand(SimulationEvent.UPDATE),SimulationCommand(SimulationEvent.NEXT))
	for (iteration <- 0 until iterlim) {
	  println("starting iteration "+iteration)
	  val start = System.currentTimeMillis()
		Logger.start(iteration)
	   
	   val factory = BackgroundFactory(pComm, pBadBack, 1)
	   val people = factory.src.take(popSize)
	   
	   val H = Hub(pBadBack,pBadFore*pBadNormDiscount, pForeCommDiscount*pComm, popSize+1)
	   
	   val triads = CliqueAll.grouped(people.iterator,popSize, cliqueSize, Community.Family)
	   for (c <- triads if DoubleSrc.next < hConP) c.random.join(H, Community.Family)
	   BinomialMix(triads.flatten, pBlend, Community.Family)
	   

	   val terrorists = PlotClusters(-(popSize+2), pForeCommDiscount*pComm, pBadFore, clusterSize).take(clusterCount)

	   H.clusters ++= Clique(Community.Plot)(terrorists)
	   val output = (cliquer.apply(triads) :+ H) ++ terrorists 
	   val (pwEL, pwVI) = (new PrintWriter("./commsim-mix.txt"), new PrintWriter("./commsim-mix-vertex-info.txt"))
	   iGraphELWriter.write(pwEL, output).close 
	   iGraphVIWriter.write(pwVI, output).close

	   output foreach( _ start )
	   
	   for (t <- 1 to simlim) {
		   if ( !output.map( _ !! updater(t)).foldLeft(true)((res,f)=> res && (f() == "ACK")) ) println("failed UPDATE")
		   if ( !output.map( _ !! nexter(t)).foldLeft(true)((res,f)=> res && (f() == "ACK")) ) println("failed NEXT")
	   }
	   output foreach( _ ! done)
	   Logger.close
	   println("completed iteration "+iteration)
	   println("run time: "+ (System.currentTimeMillis()-start))
	}
	}
}

object Logger {
  val filter = new FilenameFilter() {
    def accept(f:File, name:String) = name.contains("current")
  }
  var pws : Seq[PrintWriter] = Seq()
  def start(step:Int=0) = {
    val others = (new File("./")).listFiles(filter)
    others.foreach( (file) => file.renameTo(new File(file.getPath.replace("current",step.toString) )) ) 
    pws = Seq("./test-hub-current.txt","./test-plot-current.txt","./test-cluster-current.txt","./test-back-current.txt") map ( s => new PrintWriter(s) )
  }
  def println(src:PersonLike,msg:String) = {
    src match {
      case h:Hub => pws(0).println(msg)
      case pl:Plotter => pws(1).println(msg)
      case cl:PlotCluster => pws(2).println(msg)
      case b:Person => pws(3).println(msg)
    }
  }
  def close = {
    pws foreach { pw => pw.flush; pw.close; }
  }
  
}
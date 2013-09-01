package edu.cap10.app

import edu.cap10.person._

import edu.cap10.graph.generators._

import edu.cap10.graph.io.igraph._

import edu.cap10.distributions._
import edu.cap10.sim.Event
import edu.cap10.sim.EventType._

import edu.cap10.person.Vocabulary._ // Good, Bad
import edu.cap10.person.Community._ // 

import scala.collection.mutable.Buffer
import scala.util.Random.shuffle

import java.io._

object Test {
  def main(args: Array[String]) = {
    val (iterlim, simlim, popSize) = (1,10,100)
    val (pBadBack, pBadFore, pBadNormDiscount, pComm, pForeCommDiscount, pBlend) = (0.01, 0.1, 0.5, 0.02, 0.5, 0.0)
    val cliqueSize = 3
    val hMeanK = 10
    val hConP = (hMeanK * cliqueSize).toDouble / popSize
    val (clusterCount,clusterSize) = (3,5)
    
    for (iteration <- 0 until iterlim) {
      println("starting iteration "+iteration)
      val start = System.currentTimeMillis()
      Logger.start(iteration)
      val factory = BackgroundFactory(pComm, pBadBack, 1)
      val people = factory.src.take(popSize).toSeq
	  val H = Hub(pBadFore,pBadBack*pBadNormDiscount, pForeCommDiscount*pComm, popSize+1)
	  
	  {
        implicit val edge = Community.Family
        val families = Clique(edge).all(shuffle(people), cliqueSize)
        for (family <- families if DoubleSrc.next < hConP) family.random <~> H
        BinomialMix(edge)(CliqueHierarchy(edge).grouped(families, cliqueSize),pBlend)
      }
	   
	   {
	       implicit val edge = Community.Work
		   val businesses = Clique(Community.Work).all(shuffle(people), cliqueSize)
		   for (business <- businesses if DoubleSrc.next < hConP) business.random <~> H
		   BinomialMix(edge)(CliqueHierarchy(edge).grouped(businesses, cliqueSize),pBlend)
	   }
	   
	   {
	       implicit val defEdge = Community.Religion
	       val pd = ProportionalDistance(Community.Family,defEdge)
		   pd( (people, (0.9, 0.3)) )
	   }
	   
	   val terrorists : Iterable[PersonLike] = 
	     PlotClusters(-(popSize+2), pForeCommDiscount*pComm, pForeCommDiscount*pBadFore, clusterSize).take(clusterCount);
	   
	   {
	     implicit val edge = Community.Plot     
	     H <~> Clique(edge).apply(terrorists);
	   }
	   
	   val output = (people :+ H) ++ terrorists 
	   val (pwEL, pwVI) = (new PrintWriter("./"+(iteration+1)+"-EL.txt"), new PrintWriter("./"+(iteration+1)+"-VI.txt"))
	   PersonEdgeWriter.apply(output)(pwEL)
	   VertexWriter.apply[Community.Value,PersonLike](output)(pwVI)

	   output foreach { _ start }
	   val done = Event(DONE,simlim,null)
	   val updater = (t:Int) => Event(UPDATE,t,null)
	   val nexter = (t:Int) => Event(NEXT,t,null)
	   for (t <- 1 to simlim) {
		   if ( !output.map( _ !! updater(t)).foldLeft(true)((res,f)=> res && (f() == ACK)) ) println("failed UPDATE")
		   if ( !output.map( _ !! nexter(t)).foldLeft(true)((res,f)=> res && (f() == ACK)) ) println("failed NEXT")
	   }
	   output foreach { _ ! done }
	   Logger.close
	   println("completed iteration "+iteration)
	   println("run time: "+ (System.currentTimeMillis()-start))
	}
	Logger.cleanup(iterlim)
	}
}

object Logger {
  val runname = "4-1"
  val filter = new FilenameFilter() {
    def accept(f:File, name:String) = name.contains("current")
  }
  var pws : Seq[PrintWriter] = Seq()
  def start(step:Int=0) = {
    cleanup(step) 
    pws = Seq("./"+runname+"-hub-current.txt","./"+runname+"-plot-current.txt","./"+runname+"-cluster-current.txt","./"+runname+"-back-current.txt") map ( s => new PrintWriter(s) )
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
  def cleanup(step:Int) = {
    val others = (new File("./")).listFiles(filter)
    val replace = step.toString
    others.foreach( (file) => file.renameTo(new File(file.getPath.replace("current",replace) )) )
  } 
  
}
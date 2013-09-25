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
    // specify various model parameters; obviously, these could be program arguments
    // but then we'd need to write a separate parser
    val (iterlim, simlim, popSize) = (100,100,100)
    val (pBadBack, pBadFore, pBadNormDiscount, pComm, pForeCommDiscount, pBlend) = (0.01, 0.1, 0.5, 0.02, 0.5, 0.0)
    val cliqueSize = 3
    val hMeanK = 10
    val hConP = (hMeanK * cliqueSize).toDouble / popSize
    val (clusterCount,clusterSize) = (1,5)
    
    for (iteration <- 0 until iterlim) {
      println("starting clique iteration "+iteration)
      val start = System.currentTimeMillis()
      val (pLogger, hLogger, sLogger) = (
          PersonLikeLog("clique-back-"+iteration),
          PersonLikeLog("clique-hub-"+iteration),
          PersonLikeLog("clique-plotter-"+iteration))
      val factory = BackgroundFactory(pComm, pBadBack, 1, pLogger)
      val people = factory.src.take(popSize).toSeq
	  val H = Hub(pBadFore,pBadBack*pBadNormDiscount, pForeCommDiscount*pComm, popSize+1, hLogger)
	  val terrorists : Seq[PersonLike] = 
	     PlotClusters(-(popSize+2), pForeCommDiscount*pComm, pForeCommDiscount*pBadFore, clusterSize, sLogger).take(clusterCount);
	   {
	     implicit val edge = Community.Plot     
	     H <~> Clique[Community.Value].apply(terrorists);
	   }

	  
	  clique(people,H,hConP,cliqueSize,Community.Family,pBlend)
	  clique(people,H,hConP,cliqueSize,Community.Work,pBlend)
	  getReligion(people)
	   	   
	   val output = (people :+ H) ++ terrorists 
	   val (pwEL, pwVI) = (new PrintWriter("./"+(iteration+1)+"-EL.txt"), new PrintWriter("./"+(iteration+1)+"-VI.txt"))
	   PersonEdgeWriter.apply(output)(pwEL)
	   VertexWriter.apply[Community.Value,PersonLike](output)(pwVI)
	   runSim(simlim,output)
	   println("completed iteration "+iteration)
	   println("run time: "+ (System.currentTimeMillis()-start))
	}
 
    for (iteration <- 0 until iterlim) {
      println("starting tree iteration "+iteration)
      val start = System.currentTimeMillis()
      val (pLogger, hLogger, sLogger) = (
          PersonLikeLog("tree-back-"+iteration),
          PersonLikeLog("tree-hub-"+iteration),
          PersonLikeLog("tree-plotter-"+iteration))
      val factory = BackgroundFactory(pComm, pBadBack, 1, pLogger)
      val people = factory.src.take(popSize).toSeq
	  val H = Hub(pBadFore,pBadBack*pBadNormDiscount, pForeCommDiscount*pComm, popSize+1, hLogger)
	  
	  tree(people,H,hConP,cliqueSize,Community.Family,pBlend)
	  tree(people,H,hConP,cliqueSize,Community.Work,pBlend)
	  getReligion(people)
	   
	   val terrorists : Seq[PersonLike] = 
	     PlotClusters(-(popSize+2), pForeCommDiscount*pComm, pForeCommDiscount*pBadFore, clusterSize, sLogger).take(clusterCount);
	   
	   {
	     implicit val edge = Community.Plot     
	     H <~> Clique[Community.Value].apply(terrorists);
	   }
	   
	   val output = (people :+ H) ++ terrorists 
	   val (pwEL, pwVI) = (new PrintWriter("./"+(iteration+1)+"-EL-alt.txt"), new PrintWriter("./"+(iteration+1)+"-VI-alt.txt"))
	   PersonEdgeWriter.apply(output)(pwEL)
	   VertexWriter.apply[Community.Value,PersonLike](output)(pwVI)
	   runSim(simlim,output)
	   println("completed iteration "+iteration)
	   println("run time: "+ (System.currentTimeMillis()-start))
	}

    
    
	}
  
//  def prepareCH(pComm:Double,
//      pBackgroundBad:Double,
//      popSize:Int,
//      pForegroundBad:Double,
//      pHBackgroundBadDiscount:Double,
//      pHNormalCommDiscount:Double,
//      which:String) :
//   (Iterable[PersonLike],Hub) = {
//      val factory = BackgroundFactory(pComm, pBackgroundBad, 1)
//      val people = factory.src.take(popSize).toSeq
//	  val H = Hub(pForegroundBad,pBackgroundBad*pHBackgroundBadDiscount, pHNormalCommDiscount*pComm, popSize+1)
//    (people,H)
//  }
  
  def clique(people:Seq[PersonLike], h:Hub, pHIntegration:Double, cliqueSize:Int, e:Community.Value, remixP:Double) : Unit = {
    implicit val edge = e
    val src = Clique[Community.Value].all(shuffle(people), cliqueSize)
	for (group <- src if DoubleSrc.next < pHIntegration) {
	  group.random <~> h
	}
	BinomialMix[Community.Value].apply((CliqueHierarchy[Community.Value].grouped(src, cliqueSize), remixP))
  }
  
  def tree(people:Seq[PersonLike], h:Hub, pHIntegration:Double, cliqueSize:Int, e:Community.Value, remixP:Double) : Unit = {
    implicit val edge = e
    val discount = pHIntegration / cliqueSize
    val src = Tree[Community.Value].apply((shuffle(people), cliqueSize))
	for (group <- src if DoubleSrc.next < discount) group <~> h
	BinomialMix[Community.Value].apply((src, remixP))
  }
  
  def getReligion(people:Seq[PersonLike]) : Unit = {
    implicit val defEdge = Community.Religion
	val pd = ProportionalDistance[Community.Value](Community.Family, defEdge)
    pd( (people, (0.9, 0.3)) )
  }
  
  private val updater = (t:Int) => Event(UPDATE,t,null)
  private val nexter = (t:Int) => Event(NEXT,t,null)
  
  def runSim(simlim:Int, pop:Iterable[PersonLike]) : Unit = {
    pop foreach { _ start }
	val done = Event(DONE,simlim,null)
	   for (t <- 1 to simlim) {
		   if ( !pop.map( _ !! updater(t)).foldLeft(true)((res,f)=> res && (f() == ACK)) ) println("failed UPDATE")
		   if ( !pop.map( _ !! nexter(t)).foldLeft(true)((res,f)=> res && (f() == ACK)) ) println("failed NEXT")
	   }
	   pop foreach { _ ! done }
  }
  
}

import edu.cap10.graph.Vertex
case class PersonLikeLog(runname:String) extends LoggerSubstrate {
  private val pw = new PrintWriter("./"+runname+".txt")
  override def println(v:Vertex[_,_], msg:(Community.Value, Vocabulary.Value, PersonLike), t:Int) {
    pw.println(v.id+" "+msg._1+" "+msg._2+" "+msg._3.id+" "+t)
  }
  override def clean = {
    pw.flush;
    pw.close;
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
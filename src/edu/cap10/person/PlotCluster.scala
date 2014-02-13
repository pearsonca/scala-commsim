package edu.cap10.person

//import edu.cap10.sim.Logger
//
//case class PlotClusters(startId : Long, pInner: Double, pOuter : Double, size : Int, logger : Logger[(Community.Value, Vocabulary.Value, PersonLike),PersonLike]) {
//  val src : Stream[PersonLike] = loop(startId)
//  private def loop(id : Long) : Stream[PlotCluster] = {
//    PlotCluster(id, pInner, pOuter, size, logger) #:: loop(id - size)
//  }
//  
//}
//
//import scala.collection.mutable.SortedSet;
//
//import edu.cap10.distributions._
//import Community.{Value => CValue, _}
//import Vocabulary.{Value => VValue, _}
//import edu.cap10.sim.EventType._
//import edu.cap10.sim.Event
//
//case class PlotCluster(id : Long, pInner: Double, pOuter : Double, size : Int, 
//    passLogger : Logger[(Community.Value, Vocabulary.Value, PersonLike),PersonLike]) extends PersonLike {
//    
//  override val logger = NoOp
//  
//  override val name = "PlotCluster"
//  override def shortToString = {
//    { members map { _.shortToString } } mkString "\n"
//  }
//  val members = SortedSet(PlotterFactory(-id, passLogger).src.take(size):_*) // these are a clique
//  
//  override def start = {
//    members foreach { _ start }
//    super.start
//  }
//  
//  override def done(t:Int) = {
//    members foreach { _ ! Event(DONE, t, null) }
//    super.done(t);
//  }
//  
//  override val edges = Map( Plot -> SortedSet[PersonLike]() )
//  
//  override def hear(msg:MType,t:Int) = {
//	(members filter (_.id != msg._3.id)).random.hear(msg,t)
//	super.hear(msg,t)
//  }
//  
//  var receivedBad = false
//  override def update(t:Int) = {
//    members foreach { _ ! Event(UPDATE, t, null) }
//    receivedBad = inbox.foldLeft(false)((res,msg) => res || (msg._2 == Bad))
//    super.update(t)
//  }
//  
//  override def test(t:Int) = {
//    members foreach { _ ! Event(TEST, t, null) }
//    super.test(t)
//  }
//  
//  override def messenger(community:CValue, what:VValue, t:Int) = Event(MSG,t,(community,what,members.random))
//  
//  private final def mayInner : Iterable[(PersonLike,Vocabulary.Value)] =
//    if (DoubleSrc.next < pInner) Seq((this,Bad)) else Nil
//  private final def mayOuter : Iterable[(PersonLike,Vocabulary.Value)] =
//    if (DoubleSrc.next < pOuter) Seq((edges(Plot).random, Bad)) else Nil
//  
//  override def messages(commType:CValue) = 
//    if (commType == Plot && receivedBad)
//    	mayInner ++ mayOuter
//    else Nil // do nothing
//}
package edu.cap10.person

import scala.actors.Actor
import scala.collection.mutable.{Set => MSet, Buffer};
import scala.collection.immutable.Stream.{continually => fill};
import scala.util.Random.shuffle

import edu.cap10.message._
import edu.cap10.clock._
import edu.cap10.utils._
import edu.cap10.distributions._
import Community.{Value => CValue, _}
import Vocabulary.{Value => VValue, _}

class BackgroundFactory(pComm:Double, pBad:Double, startId : Int = 0) {
  require(pComm >= 0 && pComm <= 1,"pComm is not a probability.")
  require(pBad >= 0 && pBad <= 1,"pBad is not a probability.")
  
  val binCache = BinomialCache(pComm) // set up a binomial distribution cache
  private def loop(i:Int) : Stream[PersonLike] =
    new Person(i, binCache, pBad) #:: loop(i+1)
  val src : Stream[PersonLike] = loop(startId)
}

object BackgroundFactory {
  def apply(pComm:Double, pBad:Double, startId:Int = 0) =
    new BackgroundFactory(pComm, pBad, startId)
}

class Person(val id:Int, binCache: BinomialCache, pBad:Double) extends PersonLike {
  override val contacts = Seq(Religion, Work, Family).zip( fill(Buffer[PersonLike]()) ).toMap
  def messages(commType:CValue) = { 
    val commContacts = contacts(commType)
    val count = if (commContacts.size !=0) binCache(commContacts.size).next else 0
    if (count != 0)
      shuffle(commContacts).take(count).map( (person) => (person,if (DoubleSrc.next < pBad) Bad else Good))
	else Iterable[(PersonLike,VValue)]()
  }  
}

class Hub(pBadSubs:Double, pBadNorms:Double, pComm:Double, id:Int)
extends Person(id, BinomialCache(pComm), pBadNorms) {
  override val contacts = Seq(Religion, Work, Family, Plot).zip( fill(Buffer[PersonLike]()) ).toMap
  def clusters = contacts(Plot)
  val badCache = BinomialCache(pBadSubs)
  override def messages(commType:CValue) = commType match {
    case Plot => 
      val badCount = badCache(clusters.size).next
      if (badCount > 0)
        shuffle(clusters).take(badCount) zip Stream.continually(Bad)
      else
        Buffer()
    case _ => super.messages(commType)
  }
}

object Hub {
  def apply(pBadSubs:Double, pBadNorms:Double, pComm:Double, id:Int) = 
    new Hub(pBadSubs, pBadNorms, pComm, id)
}

class PlotterFactory(startId : Int = 0) {
  private def loop(i:Int) : Stream[PersonLike] =
    new Plotter(i) #:: loop(i+1)
  val src : Stream[PersonLike] = loop(startId)
}

object PlotterFactory {
  def apply(startId:Int) = new PlotterFactory(startId)
}

class Plotter(val id : Int) extends PersonLike { 
	override val contacts = Map[Community.Value,Buffer[PersonLike]]()	
	override def messages(commType:CValue) = Buffer() // actually do nothing but record received messages
}

object PlotClusters {
  def apply(startId : Int, pInner: Double, pOuter : Double, size : Int) = loop(startId, pInner, pOuter, size)
  private def loop(id : Int, pInner: Double, pOuter : Double, skip : Int) : Stream[PlotCluster] = {
    new PlotCluster(id, pInner, pOuter, skip) #:: loop(id - skip, pInner, pOuter, skip)
  }
}

class PlotCluster(val id : Int, val pInner: Double, val pOuter : Double, size : Int) extends PersonLike {
  
  val members = PlotterFactory(-id).src.take(size).toSeq // these are a clique
  
  override def start() = {
    members.foreach( _.start() )
    super.start()
  }
  
  override def stop = {
    members.foreach( _ ! SimulationCommand(SimulationEvent.DONE,0) )
    super.stop
  }
  
  override val contacts = Map( Plot -> Buffer[PersonLike]() )
  
  override def update(msg:Message) = {
	(members filter (_.id != msg.sender.id)).random ! msg
	super.update(msg)
  }
  
  var receivedBad = false
  override def update(t:Int) = {
    members foreach { _ ! SimulationCommand(SimulationEvent.NEXT, t) }
    receivedBad = inbox.foldLeft(false)((res,msg) => res || (msg.content == Bad))
    super.update(t)
  }
  
  override def testEvent(t:Int) = {
    members foreach { _ ! SimulationCommand(SimulationEvent.TEST, t) }
    super.testEvent(t)
  }
  
  override def messenger(community:CValue, what:VValue, t:Int) = Message(members.random,community,what,t)
  
  override def messages(commType:CValue) = if (commType == Plot && receivedBad) {
	val buffer = Buffer[(PersonLike,VValue)]()
	  
	  if (DoubleSrc.next < pInner) { // maybe send a member of inner circle bad message
	    buffer += { (this, Bad) }
	  }
	  if (DoubleSrc.next < pOuter) { // maybe send another hub a bad message
	    buffer += { (contacts(Plot).random, Bad) }
	  }
	buffer
  } else Map() // do nothing
}
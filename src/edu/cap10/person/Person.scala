package edu.cap10.person

import scala.actors.Actor
import scala.collection.mutable.{Set => MSet, Buffer};
import scala.collection.immutable.Stream.{continually => fill};
import scala.util.Random.shuffle

//import edu.cap10.channels.Path
import edu.cap10.message._
//import edu.cap10.sim._
import edu.cap10.clock._
import edu.cap10.utils._
import edu.cap10.distributions._
import Community.{Value => CValue}
import Community._
import Vocabulary.{Value => VValue}
import Vocabulary._


class BackgroundFactory(pComm:Double, pBad:Double, startId : Int = 0) {
  require(pComm >= 0 && pComm <= 1,"pComm is not a probability.")
  require(pBad >= 0 && pBad <= 1,"pBad is not a probability.")
  
  val binCache = BinomialCache(pComm)
  val src : Stream[PersonLike] = {
    def loop(i:Int) : Stream[PersonLike] = {
      new Person(i, binCache, pBad) #:: loop(i+1)
    }
    loop(startId)
  }
}

object BackgroundFactory {
  def apply(pComm:Double, pBad:Double) = new BackgroundFactory(pComm, pBad)
}

class Person(val id:Int, binCache: BinomialCache, pBad:Double) extends PersonLike {
  override val contacts = Seq(Religion, Work, Family).zip( fill(Buffer[PersonLike]()) ).toMap
        
	def messages() = {
	  { for ( community <- contacts.keys; // for each possible community
			  commContacts = contacts(community); // fish out the contacts
			  count = binCache(commContacts.length).next; // figure out how many messages to send to this community
			  if count > 0) // if that # is positive
	    yield
	    	community -> 
	  		shuffle(commContacts).take(count).map( (person) => (person,if (DoubleSrc.next < pBad) Bad else Good))
	  }.toMap
	}
  
}

class Hub(pBadSubs:Double, pBadNorms:Double, pComm:Double, id:Int) extends Person(id, BinomialCache(pComm), pBadNorms) {
  override val contacts = Seq(Religion, Work, Family, Plot).zip( fill(Buffer[PersonLike]()) ).toMap
  
  val clusters = Buffer[PlotCluster]()
  val badCache = BinomialCache(pBadSubs)
  override def messages() = {
      val res = super.messages
      val badCount = badCache(clusters.size).next
      if (badCount > 0) {
        res + (Plot -> { 
          shuffle(clusters).take(badCount) zip Stream.continually(Bad)
        })
      } else res
  }
}

object Hub {
  def apply(pBadSubs:Double, pBadNorms:Double, pComm:Double, id:Int) = new Hub(pBadSubs, pBadNorms, pComm, id)
}

class PlotterFactory(pInner:Double, pOuter:Double, startId : Int = 0) {
  require(pInner >= 0 && pInner <= 1,"pInner is not a probability.")
  require(pOuter >= 0 && pOuter <= 1,"pOuter is not a probability.")
  
  val src : Stream[PersonLike] = {
    def loop(i:Int) : Stream[PersonLike] = {
      new Plotter(i, pInner, pOuter) #:: loop(i+1)
    }
    loop(startId)
  }
}

object PlotterFactory {
  def apply(pInner:Double, pOuter:Double, startId:Int) 
  	= new PlotterFactory(pInner, pOuter, startId)
}

class Plotter(val id : Int, val pInner: Double, val pOuter : Double) extends PersonLike { 
	override val contacts = Map[Community.Value,Buffer[PersonLike]]()	
	override def messages = Map() // actually do nothing but record received messages
}

object PlotClusters {
  def apply(startId : Int, pInner: Double, pOuter : Double, size : Int) = loop(startId, pInner, pOuter, size)
  private def loop(id : Int, pInner: Double, pOuter : Double, skip : Int) : Stream[PlotCluster] = {
    new PlotCluster(id, pInner, pOuter, skip) #:: loop(id - skip, pInner, pOuter, skip)
  }
}

class PlotCluster(val id : Int, val pInner: Double, val pOuter : Double, size : Int) extends PersonLike {
  
  val members = PlotterFactory(pInner,pOuter,-id).src.take(size).toSeq // these are a clique
  
  override def start() = {
    members.foreach( _.start() )
    super.start();
  }
  
  override def stop = {
    members.foreach( _ ! "DONE" )
    super.stop
  }
  
  override val contacts = Map( Plot -> Buffer[PersonLike]() )
  var receivedBad = false
  override def update(msg:Message) = {
	receivedBad |= msg.content == Bad
	(members filter (_.id != msg.sender.id)).random ! msg
  }
  
  override def update(t:Int) = {
    members foreach { _ ! SimulationCommand(SimulationEvent.NEXT, t) }
    super.update(t)
  }
  
  override def testEvent(t:Int) = {
    members foreach { _ ! SimulationCommand(SimulationEvent.TEST, t) }
    super.testEvent(t)
  }
  
  override def messenger(community:CValue, what:VValue) = Message(members.random,community,what)
  
  override def messages = if (receivedBad) {
	receivedBad = false
	val buffer = Buffer[(PersonLike,VValue)]()
	  
	  if (DoubleSrc.next < pInner) { // maybe send a member of inner circle bad message
	    buffer += { (this, Bad) }
	  }
	  if (DoubleSrc.next < pOuter) { // maybe send another hub a bad message
	    buffer += { (contacts(Plot).random, Bad) }
	  }
	  
	if (!buffer.isEmpty) {
	   Map( Plot -> buffer )
	} else Map()
  } else Map() // do nothing
}
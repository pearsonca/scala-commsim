package edu.cap10.person

import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.{Set => MSet, Buffer};
//import scala.collection.mutable.{Map => MMap};
import scala.util.Random.shuffle

//import edu.cap10.channels.Path
import edu.cap10.message._
//import edu.cap10.sim._
import edu.cap10.clock._
import edu.cap10.utils._
import edu.cap10.distributions._
import Community.{Value => CommunityType}
import Community._


class BackgroundFactory(pComm:Double, pBad:Double, startId : Int = 0) {
  require(pComm >= 0 && pComm <= 1,"pComm is not a probability.")
  require(pBad >= 0 && pBad <= 1,"pBad is not a probability.")
  
  val binCache = BinomialCache(pComm)
  val src : Stream[PersonLike] = {
    def loop(i:Int) : Stream[PersonLike] = {
      new PersonLike {
        val id = i
        override val contacts = Map(
			Religion -> Buffer[PersonLike](),
			Work -> Buffer[PersonLike](),
			Family -> Buffer[PersonLike]()
        )

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
      } #:: loop(i+1)
    }
    loop(startId)
  }
}

/**
 * This class provides a convenient wrapper to sub-ordinate clusters.  The Hub
 * or other clusters can send to a cluster, and it takes care of the send-to-random
 * member part.
 */
//class PlotterCluster extends Buffer[Plotter] {
//  val delegate = Buffer[Plotter]()
//  def listener = delegate(IntRangeSrcCache(this.length).next)
//  
//  def update(n:Int,newelem:Plotter) = delegate.update(n, newelem)
//  def +=(p:Plotter) = { delegate += p; this }
//  def apply(n:Int) = delegate(n)
//  def clear = delegate.clear
//}
//object PlotterCluster {
//  def apply() = new PlotterCluster 
//}

class Plotter(val id : Int, val pInner: Double, val pOuter : Double) extends PersonLike {
	// funky type inference here - 
	override val contacts = Map( Plot -> Buffer[PersonLike]() )
	
	val otherClusters = Buffer[Seq[Plotter]]()
	
	// TODO handle setting receivedBad on update(msg)
	var receivedBad = false
	override def update(msg:Message) {
	  receivedBad |= msg.content == Bad
	  super.update(msg)
	}
	
	override def messages = if (receivedBad) {
	  receivedBad = false
	  val buffer = Buffer[(PersonLike,Vocabulary)]()
	  
//	  if (DoubleSrc.next < pInner) { // maybe send a member of inner circle bad message
//	    buffer += { (cluster(IntRangeSrcCache(cluster.length).next), messenger(Family,Bad)) } // TODO what community type should the message be?
//	  }
//	  if (DoubleSrc.next < pInner) { // maybe send another hub a bad message
//	    otherClusters(IntRangeSrcCache(otherClusters.length).next).listener ! messenger(Family,Bad)
//	  }
	  
	  if (!buffer.isEmpty) {
	    Map( Plot -> buffer )
	  } else Map()
	  
	} else Map() // do nothing
}

//class TestPerson extends Actor {
//  val others : Seq[Path];
//  def act() = {
//    loop {
//      react {
//        case msg => println("received")
//      }
//    }
//  }
//}
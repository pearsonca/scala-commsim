package edu.cap10.person

import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.{Set => MSet};
import scala.collection.mutable._;
import scala.util.Random.shuffle

import edu.cap10.channels.Path
import edu.cap10.message._
//import edu.cap10.sim._
import edu.cap10.clock._
import edu.cap10.utils._
import edu.cap10.distributions._


class BackgroundFactory(pComm:Double, pBad:Double) {
  require(pComm >= 0 && pComm <= 1,"pComm is not a probability.")
  require(pBad >= 0 && pBad <= 1,"pBad is not a probability.")
  
  val binCache = BinomialCache(pComm)
  val src : Stream[PersonLike] = {
    def loop(i:Int) : Stream[PersonLike] = {
      new PersonLike {
        val id = i
        def messages() = {
          val res = { for (community <- contacts.keys; 
        		  commContacts = contacts(community);
        		  count = binCache(commContacts.length).next;
        		  if count != 0) yield 
            community -> shuffle(commContacts).take(count)
          }.toMap
          Map[CommunityType,Buffer[(Int,Vocabulary)]]()
        }
      } #:: loop(i+1)
    }
    loop(0)
  }
}

class Plotter(val id : Int) extends PersonLike {
	def messages = Map[CommunityType,Buffer[(Int,Vocabulary)]]()
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
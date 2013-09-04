package edu.cap10.person

import scala.collection.mutable.SortedSet;
import scala.collection.immutable.Stream.{continually => fill};
import scala.util.Random.shuffle

import edu.cap10.distributions._
import Community.{Value => CValue, _}
import Vocabulary.{Value => VValue, _}

case class BackgroundFactory(pComm:Double, pBad:Double, startId : Int = 0, substrate:LoggerSubstrate) {
  require(pComm >= 0 && pComm <= 1,"pComm is not a probability.")
  require(pBad >= 0 && pBad <= 1,"pBad is not a probability.")
  
  val binCache = BinomialCache(pComm) // set up a binomial distribution cache
  private def loop(i:Int) : Stream[PersonLike] = new Person(i, binCache, pBad, substrate) #:: loop(i+1)
  val src : Stream[PersonLike] = loop(startId)
}

class Person(val id:Long, binCache: BinomialCache, pBad:Double, override val substrate:LoggerSubstrate) extends PersonLike {
  
  override val name = "Person"
  
  override val edges = Seq(Religion, Work, Family).zip( edgeCollSrc ).toMap
  def messages(commType:CValue) = { 
    val commContacts = edges(commType)
    val count = if (commContacts.size !=0) binCache(commContacts.size).next else 0
    if (count != 0) // TODO better way to get a random shuffle of sortedset
      shuffle(commContacts.toSeq).take(count).map( (person) => (person,if (DoubleSrc.next < pBad) Bad else Good))
	else Iterable[(PersonLike,VValue)]()
  }  
}
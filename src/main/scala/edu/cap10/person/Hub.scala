package edu.cap10.person

//import edu.cap10.distributions._
//import Community.{Value => CValue, _}
//import Vocabulary.{Value => VValue, _}
//
//import scala.collection.immutable.Stream.{continually => fill};
//import scala.collection.mutable.SortedSet;
//import scala.util.Random.shuffle

//import edu.cap10.sim.Logger
//
//case class Hub(pBadSubs:Double, pBadNorms:Double, pComm:Double, override val id:Long, 
//    override val logger:Logger[(Community.Value, Vocabulary.Value, PersonLike),PersonLike])
//extends Person(id, BinomialCache(pComm), pBadNorms, logger) {
//  override val name = "Hub"
//      
//  override val edges = Seq(Religion, Work, Family, Plot).zip( edgeCollSrc ).toMap
//  def clusters = edges(Plot)
//  val badCache = BinomialCache(pBadSubs)
//  override def messages(commType:CValue) = commType match {
//    case Plot => 
//      badCache(clusters.size).next match {
//        case 0 => Nil
//        case badCount => shuffle(clusters).take(badCount) zip Stream.continually(Bad)
//      }
//    case _ => super.messages(commType)
//  }
//}
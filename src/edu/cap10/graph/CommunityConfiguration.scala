package edu.cap10.graph

import edu.cap10.person._
import edu.cap10.distributions._
import scala.util.Random.shuffle
import scala.collection.mutable.Buffer

trait CommunityConfiguration {
  
	def commType : Community.Value
  
	def apply(pIter: Iterator[PersonLike], size:Int) : Iterable[PersonLike] 
	
}

trait CommunityConfigurationLike extends CommunityConfiguration {

	override def apply(pIter: Iterator[PersonLike], size:Int) = {
	  val people = pIter.take(size).toSeq
	  for (src <- 0 until size; tar <- connections(src)) people(src).contacts(commType) += people(tar)
	  people
	}
	
	def connections : Seq[_ <: Iterable[Int]]
  
}

class Clique(val commType:Community.Value = Community.Plot) 
extends CommunityConfiguration {
	override def apply(pIter : Iterator[PersonLike], size:Int) = {
	  val people = pIter.take(size).toSeq
	  for (src <- people) src.contacts(commType) ++= people filter (_ != src)
	  people
	}
}

object Clique {
  def apply(commType:Community.Value) = new Clique(commType)
}

class CliqueAll(cliqueSize:Int = 3, val commType:Community.Value)
extends CommunityConfiguration {
  
  override def apply(src: Iterator[PersonLike], size:Int) = {
    CliqueAll.grouped(src,size,cliqueSize,commType) flatten
  }
  
}

object CliqueAll {
  def apply(cliqueSize:Int, commType:Community.Value) = new CliqueAll(cliqueSize,commType)
  def grouped(src: Iterator[PersonLike], size:Int, cliqueSize:Int, commType:Community.Value) : Seq[_ <: Seq[PersonLike]] = 
    if (size < cliqueSize) // if there are fewer people than the clique size,
        // clique to that smaller size, and then return the smaller group
        // TODO: should not run if size == 0 ?
        Seq.fill(size)( Clique(commType)(src,size) )
    else size % cliqueSize match { // otherwise, consider the remainder from size / clique size
      case 0 => // if it's an exact fit, just make cliques
          Seq.fill(size / cliqueSize)( Clique(commType)(src,cliqueSize) )
      case rem if rem < (size / cliqueSize) =>
          // if there's too many left over to evenly increase (some) other cliques,
          // make one small clique, and the rest as requested
          grouped(src, size - rem, cliqueSize, commType) ++ grouped(src, rem, cliqueSize, commType)
      case rem =>
          // otherwise, divy up the rem up by adding an extra person to some cliques
          grouped(src, rem*(cliqueSize+1), cliqueSize+1, commType) ++ grouped(src, size - rem*(cliqueSize+1), cliqueSize, commType )
    }


}

class CliqueUp(cliqueSize:Int = 3, val commType:Community.Value)
extends CommunityConfiguration {
  override def apply(src:Iterator[PersonLike], size:Int) =
    CliqueUp.grouped(shuffle( CliqueAll.grouped(src, size, cliqueSize, commType) ), cliqueSize, commType)
  
}

object CliqueUp {
  def apply(cliqueSize:Int, commType:Community.Value) = new CliqueUp(cliqueSize,commType)
  def grouped(src: Seq[_ <: Seq[PersonLike]], cliqueSize:Int, commType:Community.Value) : Seq[PersonLike] =
    if (src.size == 1)
      src(0)
    else if (src.size < cliqueSize) {
      up(src.iterator, commType, src.size)
    } else src.size % cliqueSize match {
      case 0 => grouped(Seq.fill(src.size / cliqueSize)( up(src.iterator, commType, cliqueSize) ), cliqueSize, commType )
      case rem if rem < (src.size / cliqueSize) =>
        val iter = src.iterator
        grouped( up(iter, commType, rem) +: Seq.fill(src.size / cliqueSize)( up(iter, commType, cliqueSize) ), cliqueSize, commType)
      case rem =>
        val iter = src.iterator
        grouped( Seq.fill(rem)( up(iter, commType, cliqueSize+1) ) ++ Seq.fill((src.size/cliqueSize) - rem)( up(iter, commType, cliqueSize) ), cliqueSize, commType)
    }
  
  def up(src: Iterator[_ <: Seq[PersonLike]], commType:Community.Value, cliqueSize:Int) = {
    val res = src.take(cliqueSize).toSeq
    for (	srci <- 0 until res.size; 
    		tarj <- 0 until res.size if tarj != srci;
    		srcp = res(srci)(IntRangeSrcCache(res(srci).size).next);
    		tarp = res(tarj)(IntRangeSrcCache(res(tarj).size).next)
    	) srcp.contacts(commType) += tarp;
    res flatten
  }
}


//class FromGraphML
//class FromDot

//class Fractal(override val size:Int, val commType:Community.Value, val startId : Int = 0)
//extends CommunityConfiguration {
//	override val connections = { 
//	  // make triad or quartet cliques
//	  val range = startId until (size+startId)
//	  for (src <- range) yield {
//		  range.filter( _ != src )
//	  } 
//	}
//	private def baselink(size:Int, startId:Int) = new Clique(size,commType,startId)
//	
//	private def link( groups:Iterable[_ <: Iterable[Int]] ) = {
//	  var ref = groups flatten
//	  // pick one from each group, link them
//	  
//	}
//}


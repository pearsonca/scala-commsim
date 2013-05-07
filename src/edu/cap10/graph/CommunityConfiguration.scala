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
    val res = Buffer[PersonLike]()    
    size match {
      case 0 => res
      case size if size < cliqueSize => Clique(commType)(src,size)
      case size => size % cliqueSize match {
        case 0 => (1 to (size / cliqueSize)).foreach( res ++= Clique(commType)(src,cliqueSize) ); res
        case rem => {
          (1 to rem).foreach( res ++=  Clique(commType)(src,cliqueSize+1) )
          res ++ apply(src, size - rem*(cliqueSize+1) )
        }
      }
    }
    
  }
  
}

class CliqueUp(cliqueSize:Int = 3, val commType:Community.Value)
extends CommunityConfiguration {
  override def apply(src:Iterator[PersonLike], size:Int) = {
    // clique everything, yielding attachment points
    val res = Buffer[PersonLike]()
    res
  }
  
  private def combine(cliques: Seq[Buffer[PersonLike]]) = {
    for (src <- 0 until cliques.size; srcLim = cliques(src).size;
    		tar <- 0 until cliques.size if tar != src; tarLim = cliques(tar).size) {
      val srcPerson = cliques(src)(IntRangeSrcCache(srcLim))
      // random item from src to random item from tar
      
    }
  }
}

class FromGraphML
class FromDot

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


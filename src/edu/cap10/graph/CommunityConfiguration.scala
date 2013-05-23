package edu.cap10.graph

import edu.cap10.person._
import edu.cap10.distributions._
import scala.util.Random.shuffle
import scala.collection.mutable.Buffer
import scala.collection.Seq.{fill => repeat}

trait CommunityConfiguration {
  
	def apply[P <: PersonLike](pIter: Iterator[P], size:Int) : Iterable[P] 
	
}

class Clique(val commType:Community.Value = Community.Plot) 
extends CommunityConfiguration {
	override def apply[P <: PersonLike](pIter : Iterator[P], cliqueSize:Int) : Seq[P] = {
	  val people = pIter.take(cliqueSize).toSeq
	  for (src <- people) src.contacts(commType) ++= (people filter (_.id != src.id))
	  people
	}
	def apply[P <: PersonLike](p : Iterable[P]) : Seq[P] = apply(p.iterator, p.size)
	def add[P <: PersonLike](orig:Iterable[P], add:P) = {
	  for (p <- orig) {
	    p.contacts(commType) += add
	    add.contacts(commType) += p
	  }
	  orig ++ Seq(add)
	}
}

object Clique { def apply(commType:Community.Value) = new Clique(commType) }

class CliqueAll(cliqueSize:Int = 3, val commType:Community.Value)
extends CommunityConfiguration {
  
  override def apply[P <: PersonLike](src: Iterator[P], size:Int) = CliqueAll.grouped(src,size,cliqueSize,commType) flatten
  
}

object CliqueAll {
  def apply(cliqueSize:Int = 3, commType:Community.Value) = new CliqueAll(cliqueSize,commType)
  def grouped[P <: PersonLike](src: Iterator[P], size:Int, cliqueSize:Int, commType:Community.Value) : Seq[_ <: Seq[P]] = 
    if (size == 0 || size == 1) Seq()
    else if (size < cliqueSize) // if there are fewer people than the clique size,
        // clique to that smaller size, and then return the smaller group
        Seq(Clique(commType)(src,size))
    else size % cliqueSize match { // otherwise, consider the remainder from size / clique size
      case 0 => // if it's an exact fit, make cliques
          repeat(size / cliqueSize)( Clique(commType)(src,cliqueSize) )
      case rem if rem > (size / cliqueSize) =>
          // if there's too many left over to evenly increase (some) other cliques,
          // make one small (size = rem) clique, and the rest as requested
          grouped(src, size - rem, cliqueSize, commType) ++ grouped(src, rem, cliqueSize, commType)
      case rem =>
          // otherwise, divy up the rem up by adding an extra person to some cliques
          grouped(src, rem*(cliqueSize+1), cliqueSize+1, commType) ++ grouped(src, size - rem*(cliqueSize+1), cliqueSize, commType )
    }
}

class CliqueUp(cliqueSize:Int = 3, val commType:Community.Value)
extends CommunityConfiguration {
  override def apply[P <: PersonLike](src:Iterator[P], size:Int) = apply(CliqueAll.grouped(src, size, cliqueSize, commType))
  def apply[P <: PersonLike](src:Seq[_<:Seq[P]]) =
    CliqueUp.grouped[P](shuffle(src), cliqueSize, commType)

}

object CliqueUp {
  def apply(cliqueSize:Int = 3, commType:Community.Value) = new CliqueUp(cliqueSize,commType)
  def grouped[P <: PersonLike](src: Seq[_ <: Seq[P]], cliqueSize:Int, commType:Community.Value) : Seq[P] = {
    if (src.size == 1) // done condition for exactly divisible pieces
      src(0)
    else if (src.size <= cliqueSize) { // done condition when there are too few (or exactly the right number of) items to clique
      up(src.iterator, commType, src.size)
    } else {
      val iter = src.iterator
      val div = src.size / cliqueSize
      src.size % cliqueSize match {
	      case 0 => grouped(repeat(div)( up(iter, commType, cliqueSize) ), cliqueSize, commType )
	      case rem if rem > div =>  
	        grouped( up(iter, commType, rem) +: repeat(div)( up(iter, commType, cliqueSize) ), cliqueSize, commType)
	      case rem =>     
	        grouped( repeat(rem)( up(iter, commType, cliqueSize+1) ) ++ repeat(div - rem)( up(iter, commType, cliqueSize) ), cliqueSize, commType)
	  }
    }
  }
  def up[P <: PersonLike](src: Iterator[_ <: Seq[P]], commType:Community.Value, cliqueSize:Int) = {
    val res = src.take(cliqueSize).toSeq
    for (	srci <- 0 until res.size; 
    		tarj <- (0 until res.size).filter(_ != srci);
    		srcp = res(srci).random;
    		tarp = res(tarj).random if tarp.id != srcp.id
    	)
      srcp.contacts(commType) += tarp;
    res flatten
  }
}

object BinomialMix {
  def apply(pop:Seq[PersonLike], rate:Double, commType:Community.Value) = {
    DoubleSrc(pop.size).filter( _ < rate ).foreach( _ => { 
      // determine how many perturbations based on a binomial number of connections
      val start = pop.random
      val end = pop.random( _.id != start.id )
      val slist = start.contacts(commType)
      if (slist.contains(end)) 
        slist -= end 
      else 
        slist += end
      val elist = end.contacts(commType)
      if (elist.contains(start)) 
        elist -= start 
      else 
        elist += start
    })
    pop
  }
}

//class FromGraphML
//class FromDot

import java.io._
object iGraphELWriter {
  def write(tar:PrintWriter, pop: Seq[_<:PersonLike]) = {
    for (psrc <- pop; ctype <- psrc.contacts.keys; ptar <- psrc.contacts(ctype) ) {
      tar.println(psrc.id + " "+ptar.id + " " + ctype)
    }
    tar.flush
    tar
  }
}
object iGraphVIWriter {
  def write(tar:PrintWriter, pop: Seq[_<:PersonLike]) : PrintWriter = {
    for (psrc <- pop) { 
      if (psrc.isInstanceOf[PlotCluster]) { write(tar, psrc.asInstanceOf[PlotCluster].members) }
      writeOne(tar, psrc)
    }
    tar.flush
    tar
  }
  def writeOne(tar:PrintWriter, p:PersonLike) = {
    tar.println( p.id + " " + (p match {
	      case _:Hub => "hub"
	      case _:Plotter => "plotter"
	      case _:PlotCluster => "bombers"
	      case _ => "person"
	    }))
  }
}
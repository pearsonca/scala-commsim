package edu.cap10.distributions

import scala.math.pow
import scala.collection.LinearSeq.{fill => rep}
import scala.math.pow
import scala.runtime.RichDouble
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.immutable.PagedSeq

import java.util.Collections.{ binarySearch => jbSearch}
import java.util.{List => JList}

abstract class BinomialSrc extends DistroSrc[Int]

object BinomialSrc {
  /**
   * @param trials the number of trials
   * @param p the probability of success
   * @return the probability of i failures, (1-p)^i 
   */
  def failPs(trials:Int, p:Double) =
    successPs(trials, 1-p).reverse
    
  def successPs(trials:Int, p:Double) =
    rep(trials)(1.0).scanLeft(1.0)( _ * _ * p)
    
  def comboCoeffs(max:Int) = {
    val maxref = max-1
    var (num, den) = (1, 1)
    val coeffbase = 1 +: { for (n <- 0 until (maxref/2+maxref%2)) yield {
      num *= max - n
      den *= 1 + n
      num / den
    } }
    
    coeffbase ++ { if (maxref%2 == 0) {
      coeffbase.reverse
    } else {
      coeffbase.reverse.tail
    } }
  }
  
  private class Searchable(p:JList[RichDouble]) { 
    def apply(key:Double) = {
      val res = jbSearch(p,key)
      if (res < 0) {
        -(res+1)
      } else res
    }
  }
  private implicit def src2Searchable(p:IndexedSeq[Double]) =
    new Searchable(seqAsJavaList(p).asInstanceOf[JList[RichDouble]])
  
  def apply(max:Int, p:Double) = {
    if (max <= 0 || p < 0 || p > 1) {
      ErrorBinomialSrc
    } else if (max == 1) {
      new SingleBinomialSrc(p)
    } else {
	    val terms = src2Searchable({
	      for((m,p,c) <- 
	    		  (failPs(max,p),successPs(max,p),comboCoeffs(max)).zipped
	    	) yield m*p*c
	    }.scanLeft(0.0)(_+_).slice(1,max+1).toIndexedSeq)
	    // scanLeft adds an extra index (initial 0.0) to the max+1 cumulants, so last index == max+1
	    
	    new BinomialSrc {
	      def apply(n:Int) = {
	        val doubles = DoubleSrc(n)
	        doubles map(terms(_))
	      }
	      def next = terms(DoubleSrc.next)
	    }
    }
  }
  
  private class SingleBinomialSrc(p:Double) extends BinomialSrc {
    def apply(n:Int) = {
      val doubles = DoubleSrc(n)
      doubles map((d) => if(d < p) 1 else 0)
    }
    def next = if (DoubleSrc.next < p) 1 else 0
  }
  
  private object ErrorBinomialSrc extends BinomialSrc {
    def apply(n:Int) = throw new UnsupportedOperationException("Misconfigured BinomialSrc (max <= 0 || p < 0 || p > 1).")
    def next = throw new UnsupportedOperationException("Misconfigured BinomialSrc (max <= 0 || p < 0 || p > 1).")
  }
}


// TODO get smarter on sharing fail / success p?
class BinomialCache(p:Double) {
  private def more(buf:Array[BinomialSrc],s:Int,e:Int) = {
    for (i <- s until (s+5)) {
      println("calculating "+i)
      buf(i) = BinomialSrc(i,p)
    }
    5
  }
  val newSrc = new PagedSeq(more)
//  val src : Stream[BinomialSrc] = {
//    def error() : BinomialSrc = BinomialSrc(0,p)
//    def loop(i: Int): Stream[BinomialSrc] = {
//      println("calculating "+i)
//      BinomialSrc(i,p) #:: loop(i + 1)
//    }
//    error() #:: loop(1)
//  }
  def apply(max: Int) = newSrc(max)
}
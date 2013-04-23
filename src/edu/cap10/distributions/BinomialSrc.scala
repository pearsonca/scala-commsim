package edu.cap10.distributions

import scala.math.pow
import scala.collection.LinearSeq
import scala.math.pow
import scala.runtime.RichDouble
import scala.collection.JavaConversions.seqAsJavaList

import java.util.Collections.{ binarySearch => jbSearch}
import java.util.{List => JList}

abstract class BinomialSrc extends DistroSrc[Int]

object BinomialSrc {
  def failPs(max:Int,p:Double) =
    successPs(max,1-p).reverse
  def successPs(max:Int,p:Double) =
    LinearSeq.fill(max)(1.0).scanLeft(1.0)( _ * _ * p)
  def comboCoeffs(max:Int) = {
    val maxref = max-1
    var (num, den) = (1, 1);
    val coeffbase = 1 +: { for (n <- 0 until (maxref/2+maxref%2)) yield {
      num *= max - n
      den *= 1+n
      num / den
    } }
    
    coeffbase ++ { if (maxref%2 == 0) {
      coeffbase.reverse
    } else {
      coeffbase.reverse.tail
    } }
  }
  private class Searchable(p:JList[RichDouble]) {
    def binarySearch(key:Double) = jbSearch(p,key)
    def apply(key:Double) = {
      val res = binarySearch(key)
      if (res < 0) {
        -(res+1)
      } else res
    }
  }
  private implicit def src2Searchable(p:IndexedSeq[Double]) =
    new Searchable(seqAsJavaList(p).asInstanceOf[JList[RichDouble]])
  
  def apply(max:Int, p:Double) = {
    require(max > 0,"max arg must be greater than 0.")
    require(p >= 0 && p <= 1,"p must be a probability (0 <= p <= 1).")
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
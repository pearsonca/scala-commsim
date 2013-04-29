package edu.cap10.distributions

import scala.util.Random.nextInt

abstract class IntRangeSrc extends DistroSrcLike[Int]

object IntRangeSrc {
  private def core(mx:Int) = new Iterator[Int] {
     val hasNext = true
     def next = nextInt(mx)
  }
  private implicit def iterToSrc(iter:Iterator[Int]) = new IntRangeSrc { val inner = iter }
  
  def apply(mx: Int) : IntRangeSrc = core(mx)
  def apply(max: Int, min:Int) : IntRangeSrc = {
    require(max > min)
    if (min != 0) {
      core(max - min).map( _ + min )
    } else {
      core(max)
    }
  }
}

object IntRangeSrcCache {
  val src : Stream[IntRangeSrc] = { 
    def loop(i: Int): Stream[IntRangeSrc] = IntRangeSrc(i) #:: loop(i + 1)
    loop(0)
  }
  def apply(max: Int) = src(max)
}
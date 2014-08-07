package edu.cap10.cora

object DiscreteCountStats {
  
  def CDF(pdf:Stream[Double]) : Stream[Double] = pdf.scan(0d)(_ + _).drop(1)
  
  def poissonPDF(rate:Double) : Stream[Double] = {
    def loop(last:Double, k:Int) : Stream[Double] = {
      val v = rate*last / k
      v #:: loop(v, k+1)
    } 
    val base = Math.exp(-rate)
    base #:: loop(base, 1)
  }
  
  def poissonCDF(rate:Double) : Stream[Double] = CDF(poissonPDF(rate))
  
  def poissonK(rate:Double) : Iterator[Int] = {
    val ref = poissonCDF(rate)
    Iterator.continually({
      val draw = Math.random()
      ref.indexWhere(draw < _)
    })
  }
  
}

trait PoissonDraws {
  
  val expectedK : Double
  
  lazy val poissonK : Iterator[Int] = DiscreteCountStats.poissonK(expectedK)
  
  def nextDraw() : Int = poissonK.next()
  
}

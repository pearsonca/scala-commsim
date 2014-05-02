package edu.cap10.distributions

object DistroTest {
	def main(args: Array[String]) {
//	  val b = BinomialSrc(6,0.3)
		val cache = new BinomialCache(0.3)
	//	for (max <- 5 to 10; intgen = cache(max)) println(intgen.list(10))
	//	for (max <- 1 to 10; intgen = cache(max)) println(intgen.list(10))
		println(cache(40).list(10))
	}
}
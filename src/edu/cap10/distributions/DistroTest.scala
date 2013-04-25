package edu.cap10.distributions

object DistroTest {
	def main(args: Array[String]) {
		val cache = new BinomialCache(0.3)
		for (max <- 5 to 1; intgen = cache(max)) println(intgen.list(10)) 
		println(cache(3).list(10))
	}
}
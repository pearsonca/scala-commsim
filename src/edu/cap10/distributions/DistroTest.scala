package edu.cap10.distributions

object DistroTest {
	def main(args: Array[String]) {
		val intgen = BinomialSrc(6,0.3);
		println(intgen.list(50))
	}
}
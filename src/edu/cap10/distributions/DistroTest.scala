package edu.cap10.distributions

object DistroTest {
	def main(args: Array[String]) {
		val intgen = IntRangeSrc(10);
		println(intgen.list(10))
		println(intgen.list(10))
	}
}
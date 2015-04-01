import edu.cap10.util.PoissonGenerator

object scribble {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  implicit val rng = new scala.util.Random        //> rng  : scala.util.Random = scala.util.Random@399f9f8e
	val pg = PoissonGenerator(5)              //> pg  : edu.cap10.util.PoissonGenerator = edu.cap10.util.PoissonGenerator@e7cd
                                                  //| d92
	pg.next                                   //> res0: edu.cap10.util.NaturalInt = NaturalInt(5)
	pg.next                                   //> res1: edu.cap10.util.NaturalInt = NaturalInt(5)
	pg.next                                   //> res2: edu.cap10.util.NaturalInt = NaturalInt(3)
	pg.next                                   //> res3: edu.cap10.util.NaturalInt = NaturalInt(2)
	pg.next                                   //> res4: edu.cap10.util.NaturalInt = NaturalInt(6)
}
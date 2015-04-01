import edu.cap10.util.PoissonGenerator

object scribble {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(101); 
  println("Welcome to the Scala worksheet");$skip(43); 
  implicit val rng = new scala.util.Random;System.out.println("""rng  : scala.util.Random = """ + $show(rng ));$skip(30); 
	val pg = PoissonGenerator(5);System.out.println("""pg  : edu.cap10.util.PoissonGenerator = """ + $show(pg ));$skip(9); val res$0 = 
	pg.next;System.out.println("""res0: edu.cap10.util.NaturalInt = """ + $show(res$0));$skip(9); val res$1 = 
	pg.next;System.out.println("""res1: edu.cap10.util.NaturalInt = """ + $show(res$1));$skip(9); val res$2 = 
	pg.next;System.out.println("""res2: edu.cap10.util.NaturalInt = """ + $show(res$2));$skip(9); val res$3 = 
	pg.next;System.out.println("""res3: edu.cap10.util.NaturalInt = """ + $show(res$3));$skip(9); val res$4 = 
	pg.next;System.out.println("""res4: edu.cap10.util.NaturalInt = """ + $show(res$4))}
}

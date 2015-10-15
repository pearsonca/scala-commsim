package edu.cap10.cora.demo

import scala.annotation.tailrec
import scala.io.Source
import scala.util.parsing.combinator.RegexParsers
import scala.collection.immutable.Stream.Empty

import shared._

object MontrealReprocess {



  def parseFile(path:String) : Iterator[Observation] = for (
    	line <- Source.fromFile(path).getLines
    ) yield ObservationParser(line)

  def merge(baseStream:Stream[Observation], path2:String): Stream[Observation] = {
    val s2 = parseFile(path2).toStream

    def mergeStreams(s1: Stream[Observation], s2: Stream[Observation]): Stream[Observation] = {
      if (s1.isEmpty) s2
      else if (s2.isEmpty) s1
      else if (s1.head.start < s2.head.start) s1.head #:: mergeStreams(s1.tail, s2)
      else s2.head #:: mergeStreams(s1, s2.tail)
    }

    mergeStreams(baseStream, s2)
  }

  import java.io.{File, FilenameFilter}
  
//  def main(args:Array[String]) : Unit = {
//    val baseStream = parseFile("/Users/carlpearson/Dropbox/montreal/merged.o").toStream // args(0)
//    val mergeSrc = "/Users/carlpearson/scala-commsim/simdata" // args(1)
//    val dir = new File(mergeSrc)
//    for (src <- dir.list().filter( f => f.endsWith(".sim") )) {
//      val fw = new java.io.PrintWriter(mergeSrc+"/"+src.replace(".sim", ".cu"))
//      implicit val recorder = (obs:List[PairObs]) => {
//        obs foreach {
//          p => fw.println(p.userA+" "+p.userB+" "+p.start+" "+p.end)
//        }
//        fw.flush
//      }
//      parseFile(mergeSrc+"/"+src).foreach( o => parseOne(o, baseStream.filter(b => o.start < b.start )) )
//      fw.flush()
//      fw.close()
//    }
//    
//    // iterate over file in mergeSrc that match .sim
//    // parse(merge(basefile, simfile)) // with fw corresponding to .sim filename, but .sim replaced w/ .uu
//
//    // TODO receive a margin-of-error separate for overlaps
//    // val oname = "/Users/carlpearson/Dropbox/epidemics4share/paired.o" // args(2)
//    
//  }

  final def extract(head:Observation, tail:Stream[Observation]) : List[PairObs] = {
    tail.takeWhile(head.overlapping). // assert: no self.user + self.loc in here; if there were, they should have been consumed in preprocess
    	filter({ _.loc == head.loc }).map { intersect(_, head) }.toList
  }

  final def parseOne(x:Observation, rest:Stream[Observation])(implicit recorder: List[PairObs]=>Unit ) = {
    recorder(extract(x, rest))
  }
  
  @tailrec
  final def parse(stream:Stream[Observation])(implicit recorder: List[PairObs]=>Unit )
  	: Unit = stream match {
	    case x #:: Empty => Unit
	    case x #:: rest => {
	      recorder(extract(x, rest))
	      parse(rest)
	    }
  }

}

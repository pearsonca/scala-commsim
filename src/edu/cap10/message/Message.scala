package edu.cap10.message

import java.io._

import edu.cap10.person.Person
import edu.cap10.clock._
import edu.cap10.channels._

case class Message(topic:Int, content:Int) {
  override def toString = "concerning "+topic+", "+content
}

abstract class Logger {
  def log(msg:Message) : Unit
}

class DefaultLogger(src:Person, tar:Person) extends Logger {
  override def log(msg:Message) = print(src + " to "+tar+" : "+msg)
}

object DefaultLogger {
  def apply(src:Person,tar:Person) = new DefaultLogger(src,tar)
  def curried(src:Person)(tar:Person) = new DefaultLogger(src,tar)
}

class TimeStampedFileLogger(clock:Clock,src:Person,tar:Person,filename:String) extends Logger {
  val file = new PrintWriter(new File(filename))
  override def log(msg:Message) = {
    file println(clock.time + ":"+src+"->"+tar+":"+msg)
  }
  def shutdown = {
    file flush()
    file close()
  }
}

class PathIDClockedFL(clock:Clock, path:Path) extends Logger {
  val file = new PrintWriter(new File("path_"+path+".txt"))
  override def log(msg:Message) {
    file println(clock.time+":"+path+":"+msg)
  }
  def shutdown = {
    file flush()
    file close()
  }
}

//abstract class LogFactory {
//  def clocked(clock:Clock) : (Person,Person) => Logger
//  def apply(clock:Clock)(src:Person,tar:Person) : Logger = clocked(clock)(src,tar)
//}
//
//trait FileWriting {
//  val file : PrintWriter
//  def init(src:String) : PrintWriter = new PrintWriter(new File(src))
//  def logString(msg:String) = file println msg
//  def close = {
//    file flush()
//    file close()
//  }
//}
//
//class FileLogFactory(clock:Clock) {
//  def logFactory(src:Person) = {
//    new Logger with FileWriting {
//      file = init()
//      override def log(msg:Message) = {
//        
//      }
//    }
//  }
//}
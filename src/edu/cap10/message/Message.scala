package edu.cap10.message

import java.io._

import edu.cap10.person._
import edu.cap10.clock._
//import edu.cap10.channels._

case class Message(sender:PersonLike, community:Community.Value, content:Vocabulary.Value, t:Int) {
  override val toString = Seq(sender.id,community,content,t) mkString " "
}

abstract class Logger {
  def log(msg:Message) : Unit
  def shutdown : Boolean
  var isShutdown = false
}

//class DefaultLogger(src:Person, tar:Person) extends Logger {
//  override def log(msg:Message) = print(src + " to "+tar+" : "+msg)
//  override def shutdown = {
//    isShutdown = true
//    isShutdown
//  }
//}

//object DefaultLogger {
//  def apply(src:Person,tar:Person) = new DefaultLogger(src,tar)
//  def curried(src:Person)(tar:Person) = new DefaultLogger(src,tar)
//}

//class TimeStampedFileLogger(clock:Clock,src:Person,tar:Person,filename:String) extends Logger {
//  val file = new PrintWriter(new File(filename))
//  override def log(msg:Message) = {
//    file println(clock.time + ":"+src+"->"+tar+":"+msg)
//  }
//  override def shutdown = {
//    file flush()
//    file close()
//    isShutdown = true
//    isShutdown
//  }
//}
//
//class PathIDClockedFL(clock:Clock, pathname:String) extends Logger {
//  def this(clock:Clock,path:Path) = this(clock,path toString)
//  val file = new PrintWriter(new File("path_"+pathname+".txt"))
//  override def log(msg:Message) {
//    file println(clock.time+":"+pathname+":"+msg)
//  }
//  def shutdown = {
//    file flush()
//    file close()
//    isShutdown = true
//    isShutdown
//  }
//}
//
//abstract class LoggerFactory {
//  def create(p:Path) : Logger
//}
//
//class FileLoggerFactory(val clock:Clock, ext:String) extends LoggerFactory {
//  def this(clock:Clock) = this(clock,".txt")
//  override def create(p:Path) = {
//    val pw = new PrintWriter(new File(p+ext))
//    new Logger {
//      override def shutdown = {
//        pw flush()
//        pw close()
//        true
//      }
//      override def log(msg:Message) = {
//        pw println(p+":"+msg)
//      }
//    }
//  }
//  
//}

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
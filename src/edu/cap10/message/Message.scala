package edu.cap10.message

import edu.cap10.person.Person

case class Message(topic:Int, content:Int) {
  override def toString = "concerning "+topic+", "+content
}

abstract class Logger {
  def log : Message => Unit
}

class DefaultLogger(src:Person, tar:Person) extends Logger {
  def log = (msg:Message) => print(src + " to "+tar+" : "+msg)
}

object DefaultLogger {
  def apply(src:Person,tar:Person) = new DefaultLogger(src,tar)
}
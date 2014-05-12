package edu.cap10.cora

trait Reply

object Ack extends Reply
case class Error(msg:String) extends Reply
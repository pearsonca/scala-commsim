package edu.cap10.sunbelt

import akka.actor.ActorSystem
import akka.actor.Props

object Main extends App {
  
  val sys = ActorSystem()
  sys.actorOf(Props[Runner])

  
}
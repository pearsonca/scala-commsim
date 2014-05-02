package edu.cap10.sunbelt

import akka.actor.ActorSystem
import akka.actor.Props

object Main extends App {
  
  val sys = ActorSystem()
  val runner = sys.actorOf(Props[Runner],"runner")
  runner ! Tick(0)
  
}
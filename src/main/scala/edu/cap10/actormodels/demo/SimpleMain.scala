package edu.cap10.actormodels.demo

import akka.actor.ActorSystem
import akka.actor.TypedActor
import scala.concurrent._
import scala.concurrent.duration._

case class SimpleSystem(runConfig : SimpleParams, globalConfig : DataParams) {
  val as = ActorSystem("SimulationSystem")
  val system = TypedActor(as)
  val universe = system.typedActorOf(SimpleUniverse.props(runConfig, globalConfig))
  
  val mapper = DataEvent.generatorDay()
  
  def run = {
    val res = for (t <- 1 to globalConfig.totalDays) yield {
      Await.result( universe.tick(t), Duration(1, SECONDS)) map {
        te:TravelEvent => mapper(t,te)
      }
    }
    res.flatten
  }
  def shutdown = as.shutdown
}

object SimpleMain extends App {

  SimpleParams().parse(args.toList) match {
    case Some(config) => {
      val sim = SimpleSystem(config, MontrealProps)
      val results = sim.run
      results map { _.toString } map { println _ }
      sim.shutdown
    }
    case None => println(SimpleParams.usage)
  }

}
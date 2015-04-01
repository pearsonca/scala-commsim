package edu.cap10.actormodels.demo

import akka.actor.ActorSystem
import akka.actor.TypedActor
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

case class SimpleSystem(runConfig : SimpleParams, globalConfig : DataParams) {
  val as = ActorSystem("SimulationSystem")
  val system = TypedActor(as)
  val universe = system.typedActorOf(SimpleUniverse.props(runConfig, globalConfig))
  
  val mapper = DataEvent.generatorDay()
  
  def run = for (res <- Future.sequence(for (t <- 0 until globalConfig.totalDays) yield {
      for (tick <- universe.tick(t)) yield { tick map { mapper(t,_) } }
  })) yield res.flatten

  def shutdown = as.shutdown
}

object SimpleMain extends App {
  SimpleParams().parse(args.toList) match {
    case Some(config) => {
      val sim = SimpleSystem(config, MontrealProps)
      val results = for (res <- sim.run) yield { res map { _.toString } }
      results.onComplete { _ match {
        case Success(output) => System.out.println(output mkString System.lineSeparator)
        case Failure(e) => System.err.println(e)
      } }
      results.andThen { case _ => sim.shutdown }
      Await.result(results, Duration(30, SECONDS))
    }
    case None => println(SimpleParams.usage)
  }

}
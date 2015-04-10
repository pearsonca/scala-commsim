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
  val noActivity = Future.successful(Seq[DataEvent]())
  
  def run : Future[Seq[DataEvent]] = {
    (0 until globalConfig.totalDays).foldLeft(noActivity)( (f, t) => f flatMap {
      seq => { 
        universe.tick(t).map(next => seq ++ (next map { mapper(t,_) }))
      }
    } )
  }
  
  def shutdown = as.shutdown
}

object SimpleMain extends App {
  
  val (stdout, stderr) = (System.out, System.err)
  
  SimpleParams().parse(args.toList) match {
    case Some(config) => {
      val sim = SimpleSystem(config, MontrealProps)
      val results = for (res <- sim.run) yield { res map { _.toString } }
      results.onComplete { _ match {
        case Success(output) => {
          stdout.println(output mkString System.lineSeparator)
          stdout.flush()
        }
        case Failure(e) => stderr.println(e)
      } }
      results.andThen { case _ => sim.shutdown }
      Await.result(results, Duration(30, SECONDS))
    }
    case None => stderr.println(SimpleParams.usage)
  }

}
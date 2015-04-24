package edu.cap10.actormodels.demo

import akka.actor.ActorSystem
import akka.actor.TypedActor
import akka.actor.TypedProps
import scala.concurrent._
import edu.cap10.util.FutureUtil.FutureInputs

class SimSystem[Output](universeProps : TypedProps[TimeEvents[Output]]) {
  
  def run(timesteps : Seq[Int])(implicit ec : ExecutionContext) : Future[Seq[(Int, Seq[Output])]] = {
    val as = ActorSystem("SimulationSystem")
    val universe = TypedActor(as).typedActorOf(universeProps)
    timesteps.serialFold( day => universe.tick(day) map { (day, _) } ).andThen { case _ => as.shutdown }
  }
  
}
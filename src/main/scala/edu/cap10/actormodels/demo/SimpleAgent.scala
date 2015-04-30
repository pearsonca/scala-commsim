package edu.cap10.actormodels.demo

import edu.cap10.util.Probability
import edu.cap10.util.LocalRNG
import akka.actor.{ TypedActor, TypedActorFactory, TypedProps}

object SimpleAgent {
  
  def props(
    id : AgentID,
    haunts : Seq[LocationID],
    runConfig : SimpleParams,
    globalConfig : DataParams,
    seed : Long
  ) = TypedProps(
    classOf[Dispatchable[TravelEvent]],
    new SimpleAgent(id, haunts, globalConfig.dailyVisitProb, 60*60, seed)
  )
  
}

/*An agent that
 * - can be dispatched, and does exactly as dispatched
 * - if not dispatched, may visit somewhere on its own, with uniform random behavior
 * */
class SimpleAgent (
    id:AgentID,
    haunts:Seq[LocationID],
    dailyVisitProbability:Probability,
    meanVisitDuration:Double, // seconds
    override val seed:Long
) extends Dispatchable[TravelEvent] with LocalRNG { 
  
  private def makeVisit = rng.nextDouble < dailyVisitProbability
  
  override def _dispatch(te:TravelEvent) = 
    super._dispatch(te.copy(agentID = id))

  override def _tick(when:Int) =
    if (_dispatched || !makeVisit)
      super._tick(when)
    else {
      TravelEvent.random(id, haunts, meanVisitDuration.toInt) +: super._tick(when)
    }

}
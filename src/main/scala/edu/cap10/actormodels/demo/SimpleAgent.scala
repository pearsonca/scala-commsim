package edu.cap10.actormodels.demo

import edu.cap10.util.Probability
import scala.util.Random._

/*An agent that
 * - can be dispatched, and does exactly as dispatched
 * - if not dispatched, may visit somewhere on its own, with uniform random behavior
 * */
class SimpleAgent (
    id:AgentID,
    haunts:List[LocationID],
    dailyVisitProbability:Probability,
    aveVisitTime:Double
) extends Dispatchable[TravelEvent] {
  
  override def _dispatch(te:TravelEvent) = 
    super._dispatch(te.copy(agentID = id))

  override def _tick(when:Int) =
    if (_dispatched || (dailyVisitProbability < nextDouble))
      super._tick(when) 
    else 
      super._tick(when) :+
        TravelEvent.random(id, haunts, aveVisitTime.toInt)

}
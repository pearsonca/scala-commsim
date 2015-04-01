package edu.cap10.actormodels.demo

import edu.cap10.util.Probability
import scala.util.Random

/*An agent that
 * - can be dispatched, and does exactly as dispatched
 * - if not dispatched, may visit somewhere on its own, with uniform random behavior
 * */
class StupidAgent (
    id:AgentID,
    haunts:Seq[LocationID] = Seq(1),
    dailyVisitProbability:Probability = Probability.FALSE,
    meanVisitDuration:Double = 60, // seconds
    seed:Long = 0
) extends Dispatchable[TravelEvent] {
  
  private implicit val rng = new Random(seed) 
  
  private def makeVisit = rng.nextDouble < dailyVisitProbability
  
  override def _dispatch(te:TravelEvent) = true

  override def _tick(when:Int) = Seq(TravelEvent.random(id, haunts, meanVisitDuration.toInt))

}
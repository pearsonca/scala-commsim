package edu.cap10.actormodels.tb

import edu.cap10.util.LocalRNG

trait Entity extends LocalRNG {

  // has state
  // has (time until, state transition)
  // receives ticks, possibly with infectious interactions
  // replies with state transitions, infections generated
  
}

//trait CovertAgent extends Dispatchable[TravelEvent] with LocalRNG {
//
//  protected def id : AgentID 
//  
//  final override def _dispatch(te:TravelEvent) = 
//    super._dispatch( dispatchModifier(te).copy(agentID = id) )
//
//  final override def _tick(when:Int) =
//    timesGenerator(locationSelector) ++: super._tick(when)
//    
//  protected def timesGenerator(locations: Seq[LocationID]) : Seq[TravelEvent]
//  
//  protected def dispatchModifier(te:TravelEvent) : TravelEvent
//  
//  protected def locationSelector : Seq[LocationID]
//  
//  
//}
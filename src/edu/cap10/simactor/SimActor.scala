package edu.cap10.simactor

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef

object SimActor { // defines common elements for SimActors
  object Radicalize // become a plotter
  
  // MESSAGES ABOUT EXTERNAL ACTIVITY
  object SimEvents extends Enumeration { 
    val Bad, Good = Value
  }
  import SimEvents.{Value => SimEvent, _}

  object Relationships extends Enumeration {
    val Supervisor, Peer, Subordinate, All = Value
  }
  import Relationships.{Value => Relationship, _}
    
  object SocialContexts extends Enumeration {
    val Familial, Economic, Religious, Covert = Value
  }
  import SocialContexts.{Value => SocialContext, _}
  
  type RelationshipMap = Map[SocialContext,Map[Relationship,People]]
  type BehaviorMap = Map[SocialContext,Map[Relationship,Probability]]
  
  case class UpdateRelationships(add:RelationshipMap = Map.empty,remove:RelationshipMap = Map.empty)
  case class UpdateBehaviors(add:BehaviorMap = Map.empty, remove:BehaviorMap = Map.empty)
  
  // CREATE RELATIONSHIPS IN PARTICULAR SOCIAL CONTEXTS                   
  case class Recruit(sc:SocialContext) // add a Supervisor relationship w/ sender
  case class Peer(peers:People, sc:SocialContext) // add Peer relationship w/ peers
  case class Supervise(recruits:People, sc:SocialContext) // recruit subordinates

  case class UpdateBehavior(changes:Map[Relationship,Probability], sc:SocialContext)
  
  case class PlotterState( // the plotter state description
    superiors:Plotters = empty,
    collaborators:Plotters = empty,
    subordinates:Plotters = empty,
    probs:Map[PType.Value,Probability] = PType.values.map { (_, 0.0) } toMap,
    heardBad:Boolean = false
    // default each communication type to probability 0.0
  )

}

import SimRunner._

class SimActor extends Actor with BaseSimActor {
  import SimActor._
  import context._
  
  def receive = {
    case Radicalize => become( plotter() )
    case Recruit => become( plotter(state = PlotterState(superiors = group(sender))) )
    case _ => // ignore other messages
  }
  
  def basePlot(evolve: (PlotterState)=>Unit, state:PlotterState ) : Receive = {
    import state.{copy => change, _}
    {  case Collaborate(group) =>
         evolve(change(collaborators = collaborators ++ (group - self))) 
       
       case Subordinates(recruits) =>
         recruits foreach { recruit => recruit ! Recruit }
         evolve(change(subordinates = subordinates ++ recruits))
       
       case Recruit =>
         evolve(change(superiors = superiors + sender))
       
       case UpdateProb(changes) =>
         evolve(change(probs = probs ++ changes))
    }
  }
  
  def plotting(ps:PlotterState, time:Long) : Receive = {
    case Time(t) if t == time => // going to send some bad messages, then become a plotter
      // handle collab, etc as if plotting
  }
  
  def plotter(time:Long=0, state:PlotterState = PlotterState()) : Receive = {
    import state.{copy => change, _}
    val evolve = (newPs:PlotterState) => become( plotter(time, newPs) )
    val b = basePlot(evolve, state)
    
    def step = become( plotter(time+1, state) )
    
    val base : Receive = b orElse {
       
       case Bad if (superiors++collaborators++subordinates) contains sender => become( plotting(state,time) )
       
       case Time(t) if t == time =>
         if (random < probs(PType.Initiate)) {
           // send some bad messages based on my whole cohort
         }
         // send some Good messages
         // let me acker know what's up

    }
    simBase(step,time) orElse base
  }
}
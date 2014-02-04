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
  def simEvents = SimEvents.values
  
  object Relationships extends Enumeration {
    val Supervisor, Peer, Subordinate, All = Value
  }
  import Relationships.{Value => Relationship, _}
  def relationshipTypes = Relationships.values 
  
  object SocialContexts extends Enumeration {
    val Familial, Economic, Religious, Covert = Value
  }
  import SocialContexts.{Value => SocialContext, _}
  def socialContexts = SocialContexts.values
  
  type RelationshipMap = Map[ (SocialContext, Relationship), People]
  type BehaviorMap = Map[ (SocialContext, Relationship), Map[SimEvent, Probability]]
  
  case class UpdateRelationships(sc:SocialContext, rel:Relationship, add:People = empty, remove:People = empty)
  case class UpdateBehaviors(sc:SocialContext, rel:Relationship, event:SimEvent, p:Probability)
  
  // CREATE RELATIONSHIPS IN PARTICULAR SOCIAL CONTEXTS
  
  case class PersonState( // the plotter state description
    relationships : RelationshipMap = Map.empty,
    behaviors : BehaviorMap = Map.empty,
    time:Long = 0
  ) {
    def wholeSocialContextGroup(sc:SocialContext) : People =
      relationshipTypes.foldLeft(empty[ActorRef])((group,rel) => relationships((Covert,rel)) ++ group)
    def behaviorMapForSocialContext(sc:SocialContext) : Map[Relationship,Map[SimEvent, Probability]] =
      relationshipTypes.foldLeft(Map.empty[Relationship,Map[SimEvent,Probability]])( (res, rel) => res + (rel -> behaviors((sc,rel)) ) )
  }

}

import SimRunner._

class SimActor extends Actor with BaseSimActor {
  import SimActor._
  
  def evolve : Receive => Unit = context become _
  
  import SimActor.SimEvents.{Value => SimEvent, _}
  import SimActor.SocialContexts.{Value => SocialContext, _}
  
  def connections(ps:PersonState = PersonState())(implicit other : Receive) : Receive = {
    import ps.{copy => change, _}  
	other orElse {
	  case UpdateRelationships(sc, rel, add, remove) =>
	    this evolve connections(change(relationships = relationships + ( (sc,rel) -> (relationships((sc,rel)) ++ add -- remove) )))
	  case UpdateBehaviors(sc, rel, event, p) =>
	    this evolve connections(change(behaviors = behaviors + ( (sc,rel) -> (behaviors((sc,rel)) + (event -> p)) )))
	  case Radicalize =>
	    this evolve connections(ps)(plotter(ps))
	  case _ : SimEvent => ack
	}
  }
  
  def plotter(implicit ps:PersonState) : Receive = {
    import ps._
    val cabal = wholeSocialContextGroup(Covert)
    val plot = behaviorMapForSocialContext(Covert)
    
    {
      case Bad if cabal contains sender =>
        this evolve connections(ps)( plotting )
        ack
      case Time(t) if t == time =>
        // might send some bad message if i have a non-zero initiate probability -> aka All rel
    }
  }
  
  def plotting(implicit ps:PersonState) : Receive = {
    import ps._
    val cabal = wholeSocialContextGroup(Covert)
    val plot = behaviorMapForSocialContext(Covert)
    
    {
      case Time(t) if t == time => // have received a plot message
        val thing =
          for (relation <- relationshipTypes;
        		p = plot(relation)(Bad)) yield p
    }
  }
  
//  def receive = {
//    case Radicalize => become( plotter() )
//    case Recruit => become( plotter(state = PlotterState(superiors = group(sender))) )
//    case _ => // ignore other messages
//  }
//  
//  def basePlot(evolve: (PlotterState)=>Unit, state:PlotterState ) : Receive = {
//    import state.{copy => change, _}
//    {  case Collaborate(group) =>
//         evolve(change(collaborators = collaborators ++ (group - self))) 
//       
//       case Subordinates(recruits) =>
//         recruits foreach { recruit => recruit ! Recruit }
//         evolve(change(subordinates = subordinates ++ recruits))
//       
//       case Recruit =>
//         evolve(change(superiors = superiors + sender))
//       
//       case UpdateProb(changes) =>
//         evolve(change(probs = probs ++ changes))
//    }
//  }
//  
//  def plotting(ps:PlotterState, time:Long) : Receive = {
//    case Time(t) if t == time => // going to send some bad messages, then become a plotter
//      // handle collab, etc as if plotting
//  }
//  
//  def plotter(time:Long=0, state:PlotterState = PlotterState()) : Receive = {
//    import state.{copy => change, _}
//    val evolve = (newPs:PlotterState) => become( plotter(time, newPs) )
//    val b = basePlot(evolve, state)
//    
//    def step = become( plotter(time+1, state) )
//    
//    val base : Receive = b orElse {
//       
//       case Bad if (superiors++collaborators++subordinates) contains sender => become( plotting(state,time) )
//       
//       case Time(t) if t == time =>
//         if (random < probs(PType.Initiate)) {
//           // send some bad messages based on my whole cohort
//         }
//         // send some Good messages
//         // let me acker know what's up
//
//    }
//    simBase(step,time) orElse base
//  }
}
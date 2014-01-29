package edu.cap10.simactor

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef

object SimActor { // defines common elements for SimActors
  // MESSAGES ABOUT INTERNAL STATE
  object Radicalize                           // become a plotter
  object Recruit                              // work for a plotter
  case class Collaborate(group:Plotters)      // work with other plotters
  case class Subordinates(recruits:Plotters)  // recruit subordinates
  
  object PType extends Enumeration { // plotter behavior probabilites
    type PType = Value
    val	Initiate, // prob. of starting plot comms
        Report,   // prob. of reporting to superiors
    	Collab,	  // prob. of working w/ peers
    	Delegate  // prob. of delegating work
    	  = Value
  }
  case class UpdateProb(changes:Map[PType.Value,Probability])

  // MESSAGES ABOUT EXTERNAL ACTIVITY
  object Bad
  object Good

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
    case Radicalize =>
      context become plotter()
    case Recruit =>
      context become plotter(PlotterState(superiors = group(sender)))
    case _ => // ignore other messages
  }
  
  def plotter(ps:PlotterState = PlotterState(), time:Long=0) : Receive = {
    import ps._
    def update(newPs:PlotterState) = context become plotter(newPs, time)
    def step = context become plotter(ps,time+1)
    
    val base : Receive = {
       case Collaborate(group) =>
         update(copy(collaborators = collaborators ++ (group - self)))
       case Subordinates(recruits) =>
         recruits foreach { recruit => recruit ! Recruit }
         update(copy(subordinates = subordinates ++ recruits))
       case Recruit =>
         update(copy(superiors = superiors + sender))
       case UpdateProb(changes) =>
         update(copy(probs = probs ++ changes))
       case Bad if !heardBad => update(copy(heardBad = superiors(sender) || collaborators(sender) || subordinates(sender)))
       case Time(t) if t == time =>
         val who : People = if (heardBad || random < probs(PType.Initiate)) { // send some bad messages
           if (!superiors.isEmpty && random < probs(PType.Report)) {
             empty
           } else empty
           if (!collaborators.isEmpty && random < probs(PType.Report)) {
             empty
           } else empty
           if (!subordinates.isEmpty && random < probs(PType.Report)) {
             empty
           } else empty
         } else empty
         if (who.isEmpty) {
           
         } else {
           awaiting(who)
         }
    }
    simBase(step,time) orElse base
  }
}
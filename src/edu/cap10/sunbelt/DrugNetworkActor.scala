package edu.cap10.sunbelt

import edu.cap10.simactor.SimRunner._
import edu.cap10.simactor.AckHandler
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive

// participants have a few basic characteristics
//  - job queue size
//  - price
//  - quality of product
//  - rate of work

import scala.collection.immutable.Queue

case class Decline(want:Double)
case class Accept(paid:Double)

sealed trait Task

case class Request(pay:Double, task:Task)

class DrugNetworkActor extends Actor {

  case class GetSupplies(amt:Double) extends Task {
    def accumulate(rate:Double) = (Supplies(rate),GetSupplies(amt-rate))
  }
  case class Supplies(amt:Double) {
    def +(add:Supplies) = Supplies(amt+add.amt)
  }

  var net : Double = 0
  
  case class Supplier(
      Limit:Int = 1, // limit to working queue
      queue:Queue[(ActorRef,GetSupplies)], // working queue
      product:Supplies, // current production
      charge:Double, // percent cut
      efficiency:Double, // rate Supply input is converted into Supplies output
      conversionRate:Double) extends Receive {
    
  	override def apply: (Any) => Unit = {
  	  case Request(pay, GetSupplies(amt:Double)) if (queue.size >= Limit || pay < charge*amt) =>
  	    sender ! Decline(charge*amt)
  	  case Request(pay,s:GetSupplies) =>
  	    net += pay
  	    context become copy(queue = queue enqueue (sender,s) )
  	    sender ! Accept
  	  case t @ Tick(_) if !queue.isEmpty =>
  	    val ((replyTo, supply), left) = queue.dequeue
  	    if (supply < conversionRate) {
  	      replyTo ! (product + supply*efficiency) // start on next job?
  	      context become copy(queue = left, product = Supplies(0))
  	    } else {
  	      context become copy(
  	          queue = (replyTo, supply - conversionRate) +: left, 
  	          product = product + conversionRate*efficiency
  	      )
  	    }
  	    sender ! t.done
  	  case t @ Tick(time) =>
  	    sender ! t.done
  	    // maybe lower charge?
  	}
  	
  	override def isDefinedAt : (Any) => Boolean = {
  	  case Supply(_) => true
  	  case Tick(_) => true
  	  case _ => false
  	}
  }
  
  case class Cook()
  
  
  case class Production(nu:Double) // these actors convert drugs into cash at a certain rate
  case class Retail(nu:Double) // these actors convert drugs into cash at a certain rate
  case class Wholesale(nu:Double) // these actors partition drugs to retailers
  case class Middleman(rate:Double)
  
  case class Cash(amt:Double)
  
  def stack = context become handle(super.handlers)
  def push(r:Receive) = context become(r, false)
  
  val init : Receive = {
    case Supply(nu) =>
      stack
      push(supplier(nu))
  }
  
  def handlers = init :: super.handlers
  
  def supplier(nu:Double) : Receive = {
    case Cash(amt) =>
  }
  
}
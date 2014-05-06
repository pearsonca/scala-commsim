package edu.cap10.cora.demo

import akka.actor.ActorSystem
import akka.actor.TypedActor
import akka.actor.TypedProps

import edu.cap10.cora.StackingAgentBehavior
import edu.cap10.cora.Reply

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

object Demo {

  def main(args:Array[String]) = {
    val as = ActorSystem("Demo")
    val system = TypedActor(as)
    implicit val ec = as.dispatcher
    val (start, end) = (1, 40);
    val range = start to end
    val seven : SeventhTickHelloer = system typedActorOf(TypedProps[Seven](), "Seven")
    val five : FifthTickThanker = system typedActorOf(TypedProps[Five](), "Five")
    val sevenfive : SeventhTickHelloer with FifthTickThanker = system typedActorOf(TypedProps[SevenFive](), "75")
    val fiveseven : FifthTickThanker with SeventhTickHelloer = system typedActorOf(TypedProps[FiveSeven](),"57")
  
    val combos = for (
      i <- range;
	  tar <- List(seven, five, sevenfive, fiveseven)
    ) yield	(tar, tar.tick(i), i)
	
    for ( (tar, back, i) <- combos; res <- back ) {
      println(tar +" " + res + " on "+i)
    }

    val thing = List(seven, five, sevenfive, fiveseven) map { system stop _ }
    if (thing.reduce(_ && _))
      as.shutdown
    else {
      println("wtf?")
      as.shutdown
    }
  }
}

class Seven(override val toString:String) extends SeventhTickHelloer {
  def this() = this("Def. Seven")
}
class Five(override val toString:String) extends FifthTickThanker {
  def this() = this("Def. Five")
}
class SevenFive(override val toString:String) extends SeventhTickHelloer with FifthTickThanker {
  def this() = this("Def. 75")
}
class FiveSeven(override val toString:String) extends FifthTickThanker with SeventhTickHelloer {
  def this() = this("Def. 57")
}

case class Greeting(override val toString: String)
case class Salutation(override val toString: String)

trait SeventhTickHelloer extends StackingAgentBehavior {
  override def tick(implicit when:Int) = {
    if (when % 7 == 0) say(when, Greeting("Hello"))
    super.tick
  }
  
  def say(on:Int, h:Greeting) = println(f"Day $on : $h World, I'm the SeventhTickHelloer Trait!")
}

trait FifthTickThanker extends StackingAgentBehavior {
  override def tick(implicit when:Int) = {
    if (when % 5 == 0) say(when, Salutation("Thanks"))
    super.tick
  }
  
  def say(on:Int, s:Salutation) = println(f"Day $on : $s World, 5 days down!")
}

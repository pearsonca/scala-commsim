package edu.cap10.cora.proc

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

import edu.cap10.cora._

trait Customer extends StackingAgentBehavior {

}

trait BusinessOwner extends StackingAgentBehavior {
  
}

trait Employee extends StackingAgentBehavior {
  
}
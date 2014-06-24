package edu.cap10.cora.proc

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

import edu.cap10.cora._

trait Customer extends TimeSensitive {

}

trait BusinessOwner extends TimeSensitive {
  
}

trait Employee extends TimeSensitive {
  
}
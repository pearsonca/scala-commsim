package edu.cap10.cora

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.nextInt
import edu.cap10.util.TimeStamp

trait Travels {
  def travel(
    location:Int, hour:Int,
    min: =>Int = nextInt(60),
    sec: =>Int = nextInt(60)
  ) = Future { _travel(location, hour, min, sec)  }
  
  protected[this] def _travel(location:Int, hour:Int, min: =>Int = nextInt(60), sec: =>Int = nextInt(60)) = 
    f"$location "+TimeStamp(hour, min, sec)

}
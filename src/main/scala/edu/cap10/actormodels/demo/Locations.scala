package edu.cap10.actormodels.demo

import edu.cap10.util.TimeStamp
import edu.cap10.util.TimeStamp._

case class Locations(id:LocationID,
  open : TimeStamp = TimeStamp(Hour(8),  Minute(0), Second(0)),
  close: TimeStamp = TimeStamp(Hour(17), Minute(0), Second(0))
)
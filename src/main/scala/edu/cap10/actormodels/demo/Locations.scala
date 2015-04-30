package edu.cap10.actormodels.demo

import edu.cap10.util.TimeStamp

case class Locations(id:LocationID, open:TimeStamp = TimeStamp(8,0,0), close:TimeStamp = TimeStamp(17,0,0))
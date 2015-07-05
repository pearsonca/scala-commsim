package edu.cap10.actormodels

package object covert {

  type HotSpotID = Long
  type UserID = Long
  type Day = Long
  type Time = Long
  
  type AccessRecord = (Day, UserID, Time, Time)
  
}
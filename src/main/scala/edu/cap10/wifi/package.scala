package edu.cap10

/**
 * @author cap10
 */
package object wifi {
  type AgentID = Int
  type HotspotID = Int
  type Orders = Seq[(HotspotID, edu.cap10.util.TimeStamp)]
  type Events = Seq[Int]
  
}
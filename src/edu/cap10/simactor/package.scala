
import akka.actor.ActorRef
package object simactor {
  type Plotters = Set[ActorRef]
  type People = Set[ActorRef]
  type Sim = ActorRef
  def empty[A] = Set.empty[A]
  def group(list:ActorRef*) : Plotters = list.toSet
  type Probability = Double
  def random = Math.random
}
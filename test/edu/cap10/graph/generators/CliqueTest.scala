package edu.cap10.graph.generators

import org.scalatest.FlatSpec
import collection.mutable.Stack

class CliqueTest extends FlatSpec {

  "A Clique" should "bind every Vertex in a series with the default edge" in {
//    val stack = new Stack[Int]
//    stack.push(1)
//    stack.push(2)
//    assert(stack.pop() === 2)
//    assert(stack.pop() === 1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[String]
    intercept[NoSuchElementException] {
      emptyStack.pop()
    }
  }
  
}
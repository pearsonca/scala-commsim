package edu.cap10.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object FutureUtil {

  // enhances a sequence of inputs to be "future-foldable" without having to manage flatMap / mapping futures:
  // i.e., the inputs are provided to a function that produces a Future output
  //  the invocation of the next input depends on the resolution of the last one
  implicit class FutureInputs[Input](
    inputs:Seq[Input] // sequence to be enhanced
  )(
    implicit ec : ExecutionContext // grab execution context to make available for Future ops
  ) {
    
    private def zero[Output] = Future.successful(Seq[Output]())
    
    def serialFold[Output](
      stepFunction : (Input) => Future[Output]
    ) : Future[Seq[Output]] = 
      (zero[Output] /: inputs) {
        (done, nextInput) => done flatMap { 
          pastOutput => stepFunction(nextInput) map { 
            nextOutput => nextOutput +: pastOutput
          } 
        }
    } map { _.reverse }
  }
     
  
}
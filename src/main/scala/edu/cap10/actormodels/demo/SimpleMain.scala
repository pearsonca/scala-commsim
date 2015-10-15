package edu.cap10.actormodels.demo

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

case class SimpleSystem(runConfig : SimpleParams, globalConfig : DataParams) 
  extends SimSystem[TravelEvent](SimpleUniverse.props(runConfig, globalConfig)) {
  
  override def run(
    timesteps : Seq[Int] = (0 until globalConfig.totalDays) // provide default time steps
  )(implicit ec: ExecutionContext) = 
    super.run(timesteps) // otherwise identical
    
}

//object SimpleMain extends App {
//  
//  val (stdout, stderr) = (System.out, System.err)
//  
//  SimpleParams().parse(args.toList) match {
//    case Some(config) => {    
//      val results = for (res <- SimpleSystem(config, MontrealProps).run()) yield { 
//        res map { case (day, events) => events map { _ + day } }
//      } map { 
//        dailyEvents =>
//          stdout.println(dailyEvents mkString System.lineSeparator)
//          stdout.flush
//      }
//      results.onComplete { _ match {
//        case Success(output) => stdout.flush
//        case Failure(e) => stderr.println(e)
//      } }
//      Await.result(results, Duration(30, SECONDS))
//    }
//    case None => stderr.println(SimpleParams.usage)
//  }
//
//}
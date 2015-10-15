package edu.cap10.actormodels

package object covert {

  type HotSpotID = Long
  type UserID = Long
  type Day = Long
  type Time = Long
  
  type AccessRecord = (Day, UserID, Time, Time)
  type AccessPlan = (HotSpot, Time, Time)
  
  type CDF = Array[Double]
  type PDF = Array[Double]
  
  def pdf2CDF(pdf:PDF) : CDF = pdf.tail.scan(pdf.head)({ (a,b) => a+b })
  
  def pdfFind(pdf:Array[Double], p:Double) = {
    var in = 0
    var draw = p
    while (draw > pdf(in)) {
      draw -= pdf(in)
      in += 1
    }
    in
  }
  
  val locationMeanSrc = "./input/loc_means.csv"
  val locationShapeSrc = "./input/loc_shapes.csv"
  val locationCDFSrc = "./input/loc_cdf.csv"
  val locationPDFSrc = "./input/loc_probs.csv"
  
  def strsToDoubles(ss:Array[String]) = ss.map(_.trim).map(_.toDouble)
  
}
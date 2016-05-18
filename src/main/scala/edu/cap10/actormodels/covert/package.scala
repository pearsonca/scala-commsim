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
  
  val locationMeanSrc  = "./input/digest/filter/location_means.csv"
  val locationShapeSrc = "./input/digest/filter/location_shapes.csv"
  val locationPDFSrc   = "./input/digest/filter/location_pdf.csv"
  
  def strsToDoubles(ss:Array[String]) = ss.map(_.trim).map(_.toDouble)
  
}
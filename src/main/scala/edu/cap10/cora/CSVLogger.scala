package edu.cap10.cora

import java.io.BufferedWriter
import java.io.FileWriter

object CSVLogger {
  def makeFH(filename:String) = new BufferedWriter(new FileWriter(filename+".csv"))
}

trait CSVLoggable {
  def mkString(sep:String) : String
}

trait CSVLogger[T <: CSVLoggable] {
  
  val fh : BufferedWriter
  val sep = ", "
  
  
  
  def toRow(t:T) = t mkString sep
  
  def log(t:T) = fh.write(toRow(t)+"\n")
  
  def close = {
    fh.flush()
    fh.close()
  }
  
}

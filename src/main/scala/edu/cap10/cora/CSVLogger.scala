package edu.cap10.cora

import java.io.BufferedWriter
import java.io.FileWriter

trait CSVLogger {
  
  val fh : BufferedWriter
  val sep = ", "
  
  def makeFH(filename:String) = new BufferedWriter(new FileWriter(filename))
  
  def toRow(items:Seq[Any]) = items.mkString(sep)
  
  def log(items:Seq[Any]) = {
    fh.write(toRow(items)+"\n")
  }
  
  def close = {
    fh.flush()
    fh.close()
  }
  
}

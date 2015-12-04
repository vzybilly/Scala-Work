//Made by vzybilly.

import java.util.ArrayList
import java.io.File
import java.awt.image.BufferedImage

object JFrequencyAdder{
  def build(width:Int, height:Int, frequencies:Array[String]):BufferedImage={
    val freq:ArrayList[Double] = new ArrayList[Double]()
    for( i <- 0 until frequencies.length) {
      try { 
        freq.add(Integer.parseInt(frequencies(i)))
      } catch {
        case e: Exception => ;
      }
    }
    return generate(width, height, freq.toArray(new Array[Double](0/*Fack'n Thang, giving up.*/)))
  }
  def generate(width:Int, height:Int, frequencies:Array[Double]):BufferedImage={}
  def main(args:Array[String])={
  }
}
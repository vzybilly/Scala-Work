
import sys.process._

import javax.swing._
import java.util._
import java.awt._

import java.awt.GraphicsDevice.WindowTranslucency._

object Tester{
  //The main method.
  def main(args: Array[String])={

    // Determine what the default GraphicsDevice can support.
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val gd = ge.getDefaultScreenDevice()
    val isUniformTranslucencySupported = gd.isWindowTranslucencySupported(TRANSLUCENT)
    val isPerPixelTranslucencySupported = gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT)
    val isShapedWindowSupported = gd.isWindowTranslucencySupported(PERPIXEL_TRANSPARENT)

    println("Java v"+System.getProperty("java.version"))
    println("Uniform Trans Support "+isUniformTranslucencySupported)
    println("Per Pixel Trans Support "+isPerPixelTranslucencySupported)
    println("Shape Support "+isShapedWindowSupported)

    var frame:JFrame = new JFrame("Debug Options")
    frame.add(new JLabel("TRANSLUCENT"))
    frame.setUndecorated(true);     // Undecorates the Window
    frame.setBackground(new Color(0,0,200, 127)); //crashes if decorated!
    frame.setSize(500,500)
    frame.setShape(new java.awt.geom.Ellipse2D.Double(0,0,500,500))
    frame.setVisible(true)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    var duration = 5
    var fps = 5
    var endingX = 100
    val totes = duration * fps
    val offset = endingX / totes

    var value = 0f
    for( i <- 0 to totes) {
      value = i.toFloat/totes.toFloat
      frame.setLocation(offset*i, 5)
      frame.setOpacity(value)
      frame.repaint()
      println("value = "+value)
      Thread.sleep(1000/fps)
    }
    com.sun.awt.AWTUtilities.setWindowOpaque(frame, false)
    Thread.sleep(1500)
    frame.dispose
  }
}
//Made by vzybilly to look through the Factorio Data.Raw Structure.

import javax.swing.JEditorPane
import javax.swing.JFrame

object FactorioDataRawDumperParser{
  def main(args: Array[String])={
    buildWindow("hi")
  }
  def buildWindow(string:String)={
    val textPane:JEditorPane = new JEditorPane("text/html", "<html>"+string)
    textPane.setEditable(false)
    textPane.setDragEnabled(false)
    //Build the frame
    val frame:JFrame = new JFrame("Factorio Data.Raw Dumper Parser")
    frame.add(textPane)
    frame.setSize(500,700)
    frame.setVisible(true)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
}
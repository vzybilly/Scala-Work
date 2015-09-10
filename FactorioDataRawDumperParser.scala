//Made by vzybilly to look through the Factorio Data.Raw Structure.

import javax.swing.JEditorPane
import javax.swing.JFrame
import java.io.File

object FactorioDataRawDumperParser{
  def main(args: Array[String])={
    buildWindow(buildString(getFile(args)))
  }
  //this will check the args to see if there is a file, if not, it will ask via GUI.
  def getFile(args:Array[String]):File={
    return null
  }
  //this will build the window with the formatted HTML string.
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
  //This is the main parsing enging
  def buildString(file:File):String={
    return "hi"
  }
}
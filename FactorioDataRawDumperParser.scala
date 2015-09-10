//Made by vzybilly to look through the Factorio Data.Raw Structure.

import javax.swing.JEditorPane
import javax.swing.JFrame
import java.io.File
import java.util.Scanner
import javax.swing.JFileChooser

object FactorioDataRawDumperParser{
  def main(args: Array[String])={
    val file = getFile(args)
    if(file==null){
      println("No File.")
      System.exit(1)
    }
    val htmlString = buildString(new Scanner(file))
    if(htmlString==null){
      println("Invalid File.")
      System.exit(2)
    }
    buildWindow(htmlString)
  }
  //this will check the args to see if there is a file, if not, it will ask via GUI.
  def getFile(args:Array[String]):File={
    var file:File = null
    if(args.length > 0){
      var tempFile:File = new File(args(0))
      //checks to see if file is valid.
      if(tempFile.exists){
        if(tempFile.isFile){
          if(tempFile.canRead){
            file = tempFile
          }
        }
      }
    }
    if(file != null){
      return file
    }
    //Ask via GUI
    val chooser = new JFileChooser("Open Factorio Custom Log.")
    if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
       return chooser.getSelectedFile
    }
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
  def buildString(in:Scanner):String={
    return "hi"
  }
}
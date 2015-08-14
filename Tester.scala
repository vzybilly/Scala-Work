
import sys.process._

import javax.swing.JFrame
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkListener
import javax.swing.event.HyperlinkEvent

object Tester{
  //build a hyperlink listener that calls a function we pass to this, passing the link location to the function
  def hyperLinkListener(f:String=>Unit)=new HyperlinkListener{
    def hyperlinkUpdate(e:HyperlinkEvent){
      if(e.getEventType==HyperlinkEvent.EventType.ACTIVATED){
        f(e.getDescription)
      }
    }
  }
  def linkWorker(url:String)={
    test = !test
    textPane.setText( if (test) stringOn else  stringOff )
  }
  var test:Boolean = false
  val textPane:JEditorPane = new JEditorPane("text/html", "<html>")
  val stringOff:String = "<html><a href=\"test\">testing Off.</a>"
  val stringOn:String = "<html><a href=\"test\">testing On.</a>"
  //The main method.
  def main(args: Array[String])={
    //Test out http://docs.oracle.com/javase/7/docs/api/javax/swing/JTextPane.html Or JEditorPane
    //It might have some fun with making HTML windows in java for my entire program GUI!
    //the HTML for the window
    //Build the TextPane
    textPane.setText(stringOff)
    textPane.setEditable(false)
    textPane.setDragEnabled(false)
    textPane.addHyperlinkListener(hyperLinkListener(linkWorker))
    //Build the frame
    var frame:JFrame = new JFrame("Debug Options")
    frame.add(textPane)
    frame.setSize(500,500)
    frame.setVisible(true)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
}
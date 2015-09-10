//Made by vzybilly to look through the Factorio Data.Raw Structure.

import javax.swing.JEditorPane
import javax.swing.JFrame
import java.io.File
import java.util.Scanner
import javax.swing.JFileChooser
import java.util.ArrayList
import java.util.Collections

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
    dumpToHTMLFile("FactorioDataDump.html", htmlString)
    println("Done!")
    //buildWindow(htmlString)//Doesn't load it :<
  }
  def dumpToHTMLFile(file:String, string:String)={
    println("Making File: "+file)
    val htmlFileOutput = new java.io.PrintWriter(file)
    println("Writting File")
    htmlFileOutput.println(string)
    println("Saving File")
    htmlFileOutput.flush
    println("Closing File")
    htmlFileOutput.close
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
    println("Building GUI")
    val textPane:JEditorPane = new JEditorPane("text/html", string)
    textPane.setEditable(false)
    textPane.setDragEnabled(false)
    //Build the frame
    val frame:JFrame = new JFrame("Factorio Data Dumper Parser")
    frame.add(textPane)
    frame.setSize(500,700)
    frame.setVisible(true)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
  //This is the main parsing enging
  def buildString(in:Scanner):String={
    println("Loading File")
    val lines:ArrayList[String] = ripStrings(in)
    //clean up the line ordering, not sure if needed... 50K lines is allot to check.
    println("Sorting Lines")
    Collections.sort(lines)
    println("Parsing Lines")
    val size = getLinesSize(lines)
    println("  lines Size = "+size)
    var string:StringBuilder = new StringBuilder(size)
    while(lines.size > 0){
      //if has Period && Period is before "["
      if(lines.get(0).indexOf(".")>0 && (lines.get(0).indexOf(".") < lines.get(0).indexOf("["))){
        workBlock(lines, string, lines.get(0).substring(0,lines.get(0).indexOf(".")+1))
      }else{
        string.append(lines.remove(0) + "<br>")
      }
    }
    println("Parsing Done.")
    return "<html><head><title>Factorio Data Dump</title></head><body bgcolor=1A1A1A text=E8E8E8><h1>Data.</h1><blockquote>"+
      string.toString+"</blockquote></body></html>"
  }
  def workBlock(in:ArrayList[String], out:StringBuilder, blockID:String):Unit={
    //println("Block: "+blockID)
    out.append("<a style=\"text-decoration: underline; color: #B0B0FF\" title=\"Click to show/hide "+
      blockID+"\" type=\"button\" onclick=\"if(document.getElementById('"+
      blockID+"').style.display=='none') {document.getElementById('"+
      blockID+"').style.display=''}else{document.getElementById('"+
      blockID+"') .style.display='none'}\"> "+
      blockID+"</a><br><div id=\""+
      blockID+"\" style=\"border:1px solid #A1A1A1; padding: 5px; display:none\">")
    while(in.size > 0 && in.get(0).startsWith(blockID)){
      val current = in.get(0).substring(blockID.length)
      //println("Block.Current: "+current)
      if(current.indexOf(".")>0 && (current.indexOf(".") < current.indexOf("["))){
        workBlock(in, out, blockID+current.substring(0,current.indexOf(".")+1))
      }else{
        in.remove(0)
        out.append(current+"<br>")
      }
    }
    out.append("</div>")
  }
  def getLinesSize(arr:ArrayList[String]):Int={
    var count = 4*arr.size
    for( i <- 0 until arr.size){
      count = count + arr.get(i).length
    }
    return count
  }
  //This will rip out all strings that we care about from the scanner.
  def ripStrings(in:Scanner):ArrayList[String]={
    println("Ripping Lines")
    val prependedString = "[vzyDataDumper] - data."
    val arr:ArrayList[String] = new ArrayList[String]
    while(in.hasNext){
      var line = in.nextLine
      if(line.startsWith(prependedString)){
        arr.add(line.substring(prependedString.size).trim)
      }
    }
    println("Ripped "+arr.size+" Lines")
    return arr
  }
}
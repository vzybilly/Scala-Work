import java.awt.Component
import java.awt.Container
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout

import java.util.ArrayList
import java.util.Stack
import java.util.HashMap

import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.BoxLayout

object VZY_GUI{
  class vzyGUI(worker:MyWorker){
    def get(name:String):Component={
      return worker.names.get(name)
    }
  }

  //Builds the actual class and data from the given string.
  def build(input:String, lineSeperator:String, indentIndicator:String, hook: (String) => Unit):vzyGUI={
    return build(input.split(lineSeperator), indentIndicator, hook)
  }
  //Builds the actual class and data from the given lines.
  def build(input:Array[String], indentIndicator:String, hook: (String) => Unit):vzyGUI={
    var holder:Array[(String, Int)] = new Array[(String, Int)](input.size)
    var current:Int = 0
    for(index <- 0 until input.size){
      var line:String = input(index)
      while(line.startsWith(indentIndicator)){
        current = current + 1
        line = line.substring(indentIndicator.length)
      }
      holder(index) = (line, current)
      current = 0
    }
    return build(holder, hook)
  }
  //Uses (Line, Indent) to build the actual class and data. Input = (Line, Indent Amount)
  def build(input:Array[(String, Int)], hook: (String) => Unit):vzyGUI={
    var myWorker:MyWorker = new MyWorker(hook)
    for(parseLine <- input){
      if(!myWorker.workLine(parseLine._2, parseLine._1)){
        return null
      }
    }
    while(!myWorker.toPack.empty){
      myWorker.toPack.pop().pack()
    }
    while(!myWorker.toSetVisible.empty){
      myWorker.toSetVisible.pop().setVisible(true)
    }
    return new vzyGUI(myWorker)
  }

  private class MyWorker(hook: (String) => Unit){
    var toSetVisible:Stack[JFrame] = new Stack[JFrame]()
    var toPack:Stack[JFrame] = new Stack[JFrame]()
    var stack:Stack[Container] = new Stack[Container]()
    var holder:ArrayList[Component] = new ArrayList[Component](0)
    var names:HashMap[String, Component] = new HashMap[String, Component](0)
    var lastIndet:Int = -1

    def addAll(arr:Array[String], list:ArrayList[String])={
      for(item <- arr){
        list.add(item)
      }
    }
    def loadArgs(arg:String):ArrayList[String]={
      var args:String = arg
      var list:ArrayList[String] = new ArrayList[String](0)
      while(args.length > 0){
        args = args.trim()
        if(args.contains(" ")||args.contains("'")||args.contains("\"")){
          if(args.contains("'")||args.contains("\"")){
            var spaceIndex:Int = args.indexOf(" ")
            var quoteIndex:Int = args.length()
            var quote:String = ""
            var quoteSingleIndex:Int = args.indexOf("'")
            var quoteDoubleIndex:Int = args.indexOf("\"")
            if(quoteSingleIndex >= 0){
              quoteIndex = quoteSingleIndex
              quote = "'"
            }
            if(quoteDoubleIndex >= 0){
              if(quoteDoubleIndex < quoteIndex){
                quoteIndex = quoteSingleIndex
                quote="\""
              }
            }
            if((spaceIndex < quoteIndex)&&(spaceIndex>0)){
              list.add(args.substring(0, spaceIndex))
              args = args.substring(spaceIndex+1)
            }else{
              var tempString:String = ""
              for( fdjkslfjlskdl <- 0 until 2){
                tempString = tempString + args.substring(0, args.indexOf(quote))
                args = args.substring(args.indexOf(quote)+1)
              }
              list.add(tempString)
            }
          }else{
            addAll(args.split(" "), list)
            args = ""
          }
        }else{
          list.add(args)
          args = ""
        }
      }
      prettifyArgs(list)
      return list
    }
    def prettifyArgs(list:ArrayList[String])={
      for(index <- 0 until list.size){
        list.set(index, list.get(index).replace("=", " "))
        val ref:String = list.get(index)
        if(ref.contains(" ")){
          list.set(index, ref.substring(0, ref.indexOf(" ")).toUpperCase()+" "+ref.substring(ref.indexOf(" ")+1))
        }else{
          list.set(index, ref.toUpperCase())
        }
      }
      reArg(list)
    }
    def printArray(arr:ArrayList[_], prefix:String)={
      for(index <- 0 until arr.size){
        println(prefix+arr.get(index).toString())
      }
    }
    implicit def getNewActionListener(hookValue:String) = new java.awt.event.ActionListener{
      def actionPerformed(e: java.awt.event.ActionEvent){
        hook(hookValue)
      }
    }
    final val keyFrame:String = "FRAME"
    final val keyPanel:String = "PANEL"
    final val keyButton:String = "BUTTON"
    def reKey(key:String):String={
      if(key.equals("F")){return keyFrame}
      if(key.equals("P")){return keyPanel}
      if(key.equals("B")){return keyButton}
      return key
    }
    final val argName:String = "NAME"
    final val argText:String = "TEXT"
    final val argHook:String = "HOOK"
    final val argSize:String = "SIZE"
    final val argFlow:String = "FLOW"
    final val argGroup:String = "GROUP"
    final val argVisible:String = "VISIBLE"
    final val argFrameExit:String = "EXIT"
    def reArg(list:ArrayList[String])={
      for( listIndex <- 0 until list.size) {
        var item = list.get(listIndex)
        if(item.length==1){
          if(item.charAt(0)=='V'){list.set(listIndex, argVisible)}
        }else if(item.length > 2){
          if(item.charAt(1)==' '){
            if(item.charAt(0)=='T'){list.set(listIndex, argText+" "+item.substring(2))}else
            if(item.charAt(0)=='H'){list.set(listIndex, argHook+" "+item.substring(2))}else
            if(item.charAt(0)=='N'){list.set(listIndex, argName+" "+item.substring(2))}else
            if(item.charAt(0)=='S'){list.set(listIndex, argSize+" "+item.substring(2))}else
            if(item.charAt(0)=='G'){list.set(listIndex, argGroup+" "+item.substring(2))}else
            if(item.charAt(0)=='E'){list.set(listIndex, argFrameExit+" "+item.substring(2))}else
            if(item.charAt(0)=='F'){list.set(listIndex, argFlow+" "+item.substring(2))}
          }
        }
      }
    }

    def workLine(indent:Int, line:String):Boolean={
      var good:Boolean = true
      var key:String = ""
      var args:ArrayList[String] = new ArrayList[String](0)
      if(line.contains(" ")){
        key = line.substring(0, line.indexOf(" ")).toUpperCase()
        args = loadArgs(line.substring(line.indexOf(" ")+1))
      }else{key = line.toUpperCase()}
      key = reKey(key)

      println("  Key: \""+key+"\"")
      println("  Indent: "+indent)
      if(args.size()>0){
        println("  Args: ")
        printArray(args, "    ")
      }else{println("  Args: None.")}

      while(indent < lastIndet){
        stack.pop()
        lastIndet = lastIndet - 1
      }

      //Switch on the Keys
      if(key.equals(keyFrame)){if(!buildFrame(indent, args)){good = false}}else
      if(key.equals(keyPanel)){if(!buildPanel(indent, args)){good = false}}else
      if(key.equals(keyButton)){if(!buildButton(indent, args)){good = false}}

      lastIndet = indent
      return good
    }
    def buildButton(indent:Int, args:ArrayList[String]):Boolean={
      var me:JButton = new JButton()
      holder.add(me)
      addHelper(me, args)
      var argIndex = 0
      while(argIndex < args.size()) {
        if(args.get(argIndex).startsWith(argText)){
          me.setText(args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1))
          args.remove(argIndex)
        }else
        if(args.get(argIndex).startsWith(argHook)){
          me.addActionListener(getNewActionListener(args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1)))
          args.remove(argIndex)
        }else//Add more options here.
        {argIndex = argIndex + 1}
      }
      return true
    }
    def buildPanelStub(me:JPanel, indent:Int, args:ArrayList[String]):Boolean={
      stack.push(me)
      var argIndex = 0
      while(argIndex < args.size()) {
        if(args.get(argIndex).startsWith(argGroup)){
          me.setBorder(javax.swing.BorderFactory.createTitledBorder(args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1)))
          args.remove(argIndex)
        }else
        if(args.get(argIndex).startsWith(argFlow)){
          var flowStyle:String = args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1).toUpperCase()
          if(flowStyle.equals("DOWN")){
            me.setLayout(new BoxLayout(me, BoxLayout.Y_AXIS))
          }else if(flowStyle.equals("RIGHT")){
            me.setLayout(new BoxLayout(me, BoxLayout.X_AXIS))
          }else if(flowStyle.equals("WHATEVER")){
            me.setLayout(new FlowLayout())
          }else if(flowStyle.startsWith("GRID")){
            flowStyle = flowStyle.substring(flowStyle.indexOf(" ")+1)
            var width:Int = Integer.parseInt(flowStyle.substring(0, flowStyle.indexOf(" ")))
            flowStyle = flowStyle.substring(flowStyle.indexOf(" ")+1)
            var height:Int = Integer.parseInt(flowStyle)
            me.setLayout(new GridLayout(height, width))
          }
          args.remove(argIndex)
        }else//Add more options here.
        {argIndex = argIndex + 1}
      }
      return true
    }
    def buildFrame(indent:Int, args:ArrayList[String]):Boolean={
      var me:JFrame = new JFrame
      me.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      holder.add(me)
      var me2:JPanel = new JPanel(new BorderLayout())
      holder.add(me2)
      me.add(me2)

      var sized:Boolean = false
      var argIndex = 0
      while(argIndex < args.size()) {
        if(args.get(argIndex).startsWith(argText)){
          me.setTitle(args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1))
          args.remove(argIndex)
        }else
        if(args.get(argIndex).equals(argVisible)){
          toSetVisible.push(me)
          args.remove(argIndex)
        }else
        if(args.get(argIndex).startsWith(argFrameExit)){
          var exitType:String = args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1).toUpperCase()
          println("Frame is trying to set to: '"+exitType+"'")
          if(exitType.equals("HIDE")){
            println("Frame set to HIDE")
            me.setDefaultCloseOperation(1)
          }else if(exitType.equals("DISPOSE")){
            println("Frame set to DISPOSE")
            me.setDefaultCloseOperation(2)
          }else if(exitType.equals("NOTHING")){
            println("Frame set to NOTHING")
            me.setDefaultCloseOperation(0)
          }
          args.remove(argIndex)
        }else
        if(args.get(argIndex).startsWith(argSize)){
          var dim:Array[String] = args.get(argIndex).substring(args.get(argIndex).indexOf(" ")+1).split(" ")
          if(dim.length==2){
            me.setSize(Integer.parseInt(dim(0)),Integer.parseInt(dim(1)))
          }
          sized = true
          args.remove(argIndex)
        }else//Add more options here.
        {argIndex = argIndex + 1}
      }
      if(!sized){toPack.add(me)}
      return buildPanelStub(me2, indent, args)
    }
    def buildPanel(indent:Int, args:ArrayList[String]):Boolean={
      var me:JPanel = new JPanel(new BorderLayout())
      holder.add(me)
      addHelper(me, args)
      //this is if me == container.
      return buildPanelStub(me, indent, args)
    }
    def addHelper(item:Component, args:ArrayList[String])={
      for(index <- 0 until args.size()){
        var arg:String = args.get(index)
        if(arg.startsWith("ADD")){
          println("Add Logic here with how to add to.")
        }
      }
      stack.peek().add(item)
    }
  }

  //Just to test that things work right. Keep above classes but below the methods.
  def main(args: Array[String]):Unit={
    println("Welcome to VZY GUI.")
    var gui:vzyGUI = build(
      //"f t='My new Frame~!'s='227 50'f='whatever'v,"+
      "f t='My new Frame~!'f='grid 2 2'v,"+
      " p f='Down'g='1',"+
      "  b t='My Button 1'h='btn1',"+
      "  b t='My Button 2'h='btn2',"+
      " p f='Right'g='2',"+
      "  b t='My Button 3'h='btn3',"+
      "  b t='My Button 4'h='btn4',"+
      " p f='whatever'g='3',"+
      "  b t='My Button 5'h='btn5',"+
      "  b t='My Button six'h='btn6',"+
      " p f='Grid 1 3'g='4',"+
      "  b t='My Button 7'h='btn7',"+
      "  b t='My Button eight'h='btn8',"+
      "  b t='My Button 9'h='btn9'",
      ","," ", println)
    if(gui!=null){
      gui.get("Ben")
    }else{
      println("GUI is NULL!")
    }
  }
}
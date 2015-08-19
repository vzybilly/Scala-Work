//Made by vzybilly.
//WindowWatcher3K

import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.JSlider
import java.awt.BorderLayout

class WW3K_GUI(varls:WW3K_Varls){
  //was the shut down caused by the GUI?
  var shutDown:Boolean = false
  //the main label that gets all the programs listed and such
  val label:JLabel = new JLabel("")
  //a label at the top of the window to tell how long we've been running, mostly used to tell if the program lagged.
  val timeLabel:JLabel = new JLabel("")
  //the string to prepend to the guiTimeLabel. more is added at INIT!
  var timeLabelString:String = "started: "
  //our stock name limit for most common name of windows.
  //Load from file!
  var nameLimit:Int = 30
  //the slider that will update the name limit. see if we can add this back inline!
  val nameLimitSlider:JSlider = new JSlider(javax.swing.SwingConstants.HORIZONTAL, 10, 100, nameLimit)
  //when guiUpdateTicks is >= to this, actually update GUI
  var updateEveryXTicks:Int = 2
  //Counter for how many times the GUI has been called, resets every time it updates.
  var updateTicks:Int = 0
  //used to kill frame in the exit.
  var win:JFrame = null
  def close()={
    if(win != null){
      println("Done gui.close if(TRUE)")
      SwingUtilities.invokeLater(
        new Runnable{
          def run(){
            win.dispose
          }
        }
      )
      println("Done gui.close DISPOSE")
    }
    println("Done gui.close DONE")
  }
  def buildLater()={
    //build the window
    win = new JFrame("Window Watcher 3,000!");{
      //build the main panel of window
      val panel:JPanel = new JPanel ;{
        panel.setLayout(new BorderLayout )
        //Main Content
        val scroll = new JScrollPane(label)
        scroll.setPreferredSize(new java.awt.Dimension(650, 300))
        //Bottom Content
        val cntrlPanel:JPanel = new JPanel ;{
          cntrlPanel.setLayout(new BorderLayout )
          //Bottom Main
          val closeBtn = new JButton("Close?")
          closeBtn.addActionListener(actionListener(closeBtn))
          //Bottom Right
          val debugBtn = new JButton("Toggle Debug")
          debugBtn.addActionListener(actionListener(toggleDebug))
          //Bottom Top, name limit slider.
          val sliderPanel:JPanel = new JPanel ;{
            sliderPanel.setLayout(new BorderLayout )
            nameLimitSlider.addChangeListener(changeListener(sliderUpdate))
            nameLimitSlider.setMajorTickSpacing(5);
            nameLimitSlider.setMinorTickSpacing(1);
            nameLimitSlider.setPaintTicks(true);
            nameLimitSlider.setPaintLabels(true);
            sliderPanel.add(new JLabel("Max name length:"), BorderLayout.WEST)
            sliderPanel.add(nameLimitSlider, BorderLayout.CENTER)
          }
          //Add to Bottom Content
          cntrlPanel.add(sliderPanel, BorderLayout.PAGE_START)
          cntrlPanel.add(closeBtn, BorderLayout.CENTER)
          cntrlPanel.add(debugBtn, BorderLayout.EAST)
        }
        //Add to Main Content
        //Main
        panel.add(scroll, BorderLayout.CENTER)
        //Bottom
        panel.add(cntrlPanel, BorderLayout.PAGE_END)
        //Top (single Label.)
        panel.add(timeLabel, BorderLayout.PAGE_START)
      }
      //Finish up building the window.
      win.add(panel)
      win.pack
      //we want this to be DO_NOTHING_ON_CLOSE but it's not actually that... look up!
      win.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
      win.setVisible(true)
    }
  }
  //Build our GUI later and set up our time label string.
  def build(){
    //add junk to the time label prepending string.
    timeLabelString += new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date())+". ellapsed: "
    SwingUtilities.invokeLater(
      new Runnable{
        def run(){
          buildLater
        }
      }
    )
  }
  //Happens when the closeBTN is clicked.
  def closeBtn()={
    shutDown=true
    varls.logic.shutDownHook
  }
  //Happens when the toggleDebugBTN is clicked, toggles DEBUG, THIS WILL SPAWN WINDOW FOR ALL THE DEBUG OPTIONS!
  def toggleDebug()={
    varls.debug = !varls.debug
  }
  //Happens when the Name Limit Slider is moved, update our name limit.
  def sliderUpdate()={
    nameLimit = nameLimitSlider.getValue
  }
  //build a change listener that calls a function we pass to this.
  def changeListener(f: =>Unit)=new javax.swing.event.ChangeListener{
    def stateChanged(e:javax.swing.event.ChangeEvent){
      f
    }
  }
  //build a action listener that calls a function we pass to this.
  def actionListener(f: =>Unit)=new java.awt.event.ActionListener{
    def actionPerformed(e:java.awt.event.ActionEvent){
      f
    }
  }
  //Update the Main GUI window
  def update():Unit={
    //Move our updateTicks into range
    updateTicks %= updateEveryXTicks
    //increment the ticks.
    updateTicks += 1
    //if it's not time, return.
    if(updateTicks != 1){
      return
    }
    //this is our init string for the label. gets the headers and table made. We can't move out because it changes in the method!
    var htmlGiantString = "<html><table border=\"1\" style=\"width:100%\">"+
      "<tr><th>Most Common Name</th><th>App Name</th><th>Time Open</th><th>Time Focused</th>"
    //If we're debugging, show the WID and PID, this is for the Headers
    if(varls.debug){
      htmlGiantString += "<th>Window ID</th><th>App ID</th>"
    }
    //End the Header Row.
    htmlGiantString += "</tr>"
    //add items to our table
    for( index <- 0 until varls.windowList.size) {
      htmlGiantString=htmlGiantString+buildHTML_FromWindowWorker(varls.windowList.get(index))
    }
    //set the label to our table and close the table at the same time.
    label.setText(htmlGiantString + "</table></html>")
  }
  def buildHTML_FromWindowWorker(item:WW3K_Window):String={
    //get our most common name for this item.
    var name = item.getMostCommonTitle
    //trim if it's to long.
    if(name.length>nameLimit){name=name.substring(0,nameLimit)}
    //add the additional names if we are DEBUGGING!
    var additionalPayload:String = ""
    if(varls.debug){
      additionalPayload += "<td>"+item.ID+"</td><td>"+item.PID+"</td>"
      for( index <- 0 until item.getNameCount){
        additionalPayload = additionalPayload + "<td>" + item.getNameTotal(index) + "</td>"
      }
    }
    //return the nicely formatted HTML Table Row Entry of this item.
    return "<tr>"+
        "<td>"+name+"</td>"+
        "<td>"+item.Owner+"</td>"+
        "<td>"+buildTime(item.counter)+"</td>"+
        "<td>"+buildTime(item.foCounter)+"</td>"+
        additionalPayload+
      "</tr>"
  }
  //when given ticks, make human readable time.
  def buildTime(count:Int):String={
    //so we can do maths on it. only reason. can we add var up above?
    var ticks:Int = count
    //used to be percise with the sleepTime conversion.
    val temp:Double = ticks
    //what we return. is built from right to left! (End to begining!)
    var toReturn:String = "."
    //rip seconds and adjust ticks.
    toReturn = (ticks % (60.0 * varls.divAmt)/varls.divAmt) + toReturn; ticks = ((temp / (60.0 * varls.divAmt)).toInt)
    //rip minutes and adjust ticks.
    toReturn = ticks % 60 + ":" + toReturn; ticks = ticks / 60
    //rip hours and adjust ticks.
    toReturn = ticks % 24 + ":" + toReturn; ticks = ticks / 24
    //rip days and adjust ticks.
    if(ticks > 0){ toReturn = ticks % 7 + " day(s) " + toReturn; ticks = ticks / 7}
    //rip weeks and adjust ticks.
    if(ticks > 0){ toReturn = ticks % 52 + " week(s) " + toReturn; ticks = ticks / 52}
    //rip years and adjust ticks.
    if(ticks > 0){ toReturn = ticks+ "year(s)" + toReturn;}
    //who cares beyond this, just return the years.
    return toReturn
  }
  //Called by the main loop method, update our top label.
  def updateTime={
    var additionalPayload:String = ""
    //if we're debugging, add in the offset and the que size.
    if(varls.debug){
      additionalPayload = " Off by: " + varls.tickOffset + " Ticks. Que Size: " + varls.windowsToProccess.size
    }
    //update our time label.
    timeLabel.setText(timeLabelString + buildTime(varls.realTicks) + " from Ticks: " + buildTime(varls.ticks) + additionalPayload)
  }
}
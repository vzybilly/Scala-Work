//Made by vzybilly.

import sys.process._ //this is good as is, I think all are used anyway.
import java.util.Scanner
import java.util.ArrayList
import java.io.PrintWriter
import java.io.File
import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.JOptionPane
import javax.swing.JSlider
import java.awt.BorderLayout
import java.util.concurrent.LinkedBlockingQueue

object WindowWatcher3K {
  //the list of windows this session.
  val windowList:ArrayList[WindowWorker] = new ArrayList[WindowWorker]
  //the amount of MS to sleep
  //Load from file!
  val sleepTime:Int = 100
  //used to pull seconds from ticks. (how many ticks in a second? this many.)
  var divAmt:Double = 1000.0 / sleepTime;
  //Are we actively handling a shutdown or have already handled?
  var shuttingDown:Boolean = false
  //was the shut down caused by the GUI?
  var guiShutDown:Boolean = false
  //what was the system MS when we started?
  var startupTime:Long = -1
  //How long as the pgram been running using system time?
  var realTicks:Int = 0
  //how many runs have we had since starting?
  var ticks:Int = 0
  //How many ticks are we off by? negetive is behind!
  var tickOffset:Int = 0
  //when should we skip the sleep call and just run again to keep margin of error low?
  var skipSleepWhenBelowThisTickOffset = -.75*sleepTime
  //Are we debugging the program? (Has a GUI button to toggle!)
  var debug:Boolean = false
  //Used to pass window info from our main loop to our secondary thread!
  val windowsToProccess:LinkedBlockingQueue[RawWindowLoading] = new LinkedBlockingQueue[RawWindowLoading](42000)
  //Secondary thread used to proccess the windows and update the GUI!
  val windowsProccessor:WindowProccessingThread = new WindowProccessingThread
  //the main label that gets all the programs listed and such
  val guiLabel:JLabel = new JLabel("")
  //a label at the top of the window to tell how long we've been running, mostly used to tell if the program lagged.
  val guiTimeLabel:JLabel = new JLabel("")
  //the string to prepend to the guiTimeLabel. more is added at INIT!
  var guiTimeLabelString:String = "started: "
  //our stock name limit for most common name of windows.
  //Load from file!
  var guiNameLimit:Int = 30
  //the slider that will update the name limit. see if we can add this back inline!
  val guiNameLimitSlider:JSlider = new JSlider(javax.swing.SwingConstants.HORIZONTAL, 10, 100, guiNameLimit)
  //when guiUpdateTicks is >= to this, actually update GUI
  var guiUpdateEveryXTicks:Int = 2
  //Counter for how many times the GUI has been called, resets every time it updates.
  var guiUpdateTicks:Int = 0

  class WindowProccessingThread extends Thread{
    override def run{
      //we want this to run forevers.
      while(true){
        //windowsToProccess.take will wait till there in one to take!
        var windows: Array[WindowWorker] = buildList(windowsToProccess.take)
        //for each of our new windows, if we have it, update the old with the new, else, add it.
        //this loop runs on O(n^2) FIX IT!!!!
        for(item:WindowWorker <- windows){
          addWindowtoList(item)
        }
        //now that we did all the work with the new list of windows, update the GUI to reflect our perfection~<3
        updateGUI
      }
    }
  }

  def main(args: Array[String]) = {
    //add our shutdown hook
    //I think this broke along the way, don't care, use close button to make sure!
    Runtime.getRuntime.addShutdownHook(new Thread{override def run{WindowWatcher3K.shutDownHook}})
    //load up data file.
    //add junk to the time label prepending string.
    guiTimeLabelString+=new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date())+". ellapsed: "
    //later, build the GUI...
    later(buildGUI)
    //start up our proccessor
    windowsProccessor.start
    //when did we start?
    startupTime = System.currentTimeMillis
    //we never want to stop!... unless shutting down
    while(!shuttingDown){
      //do our loop.
      doLoop(false)
      //while we are lagging behind to hard
      while(tickOffset < skipSleepWhenBelowThisTickOffset){
        val tickBefore = ticks
        //loop to catch up!
        doLoop(true)
        println("Skipping sleep: "+tickBefore+"->"+ticks)
      }
      //this should never get a offset > afew MS! but we are, sometimes more than afew hundread~thousand!
      //is our skip sleep giving us this result? it is very odd to be having...
      //See if the bug above is still happening, see about possible fixes.

      //sleep.
      Thread.sleep(sleepTime/10)
    }
  }
  def buildGUI()={
    //build the window
    val win = new JFrame("Window Watcher 3,000!");{
      //build the main panel of window
      val panel:JPanel = new JPanel ;{
        panel.setLayout(new BorderLayout )
        //Main Content
        val scroll = new JScrollPane(guiLabel)
        scroll.setPreferredSize(new java.awt.Dimension(650, 300))
        //Bottom Content
        val cntrlPanel:JPanel = new JPanel ;{
          cntrlPanel.setLayout(new BorderLayout )
          //Bottom Main
          val closeBtn = new JButton("Close?")
          closeBtn.addActionListener(actionListener(guiCloseBtn))
          //Bottom Right
          val debugBtn = new JButton("Toggle Debug")
          debugBtn.addActionListener(actionListener(guiToggleDebug))
          //Bottom Top, name limit slider.
          val sliderPanel:JPanel = new JPanel ;{
            sliderPanel.setLayout(new BorderLayout )
            guiNameLimitSlider.addChangeListener(changeListener(guiSliderUpdate))
            guiNameLimitSlider.setMajorTickSpacing(5);
            guiNameLimitSlider.setMinorTickSpacing(1);
            guiNameLimitSlider.setPaintTicks(true);
            guiNameLimitSlider.setPaintLabels(true);
            sliderPanel.add(new JLabel("Max name length:"), BorderLayout.WEST)
            sliderPanel.add(guiNameLimitSlider, BorderLayout.CENTER)
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
        panel.add(guiTimeLabel, BorderLayout.PAGE_START)
      }
      //Finish up building the window.
      win.add(panel)
      win.pack
      //we want this to be DO_NOTHING_ON_CLOSE but it's not actually that... look up!
      win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      win.setVisible(true)
    }
  }
  //later, do this GUI thing please~
  def later(thing: =>Unit){SwingUtilities.invokeLater(new Runnable{def run(){thing}})}
  def guiCloseBtn()={guiShutDown=true;shutDownHook}
  def guiToggleDebug()={debug = !debug}
  def guiSliderUpdate()={guiNameLimit = guiNameLimitSlider.getValue}
  //build a change listener that calls a function we pass to this.
  def changeListener(f: =>Unit)=new javax.swing.event.ChangeListener{def stateChanged(e:javax.swing.event.ChangeEvent){f}}
  //build a action listener that calls a function we pass to this.
  def actionListener(f: =>Unit)=new java.awt.event.ActionListener{def actionPerformed(e:java.awt.event.ActionEvent){f}}
  //when given ticks, make human readable time.
  def buildTime(count:Int):String={
    //so we can do maths on it. only reason. can we add var up above?
    var ticks:Int = count
    //used to be percise with the sleepTime conversion.
    val temp:Double = ticks
    //what we return. is built from right to left! (End to begining!)
    var toReturn:String = "."
    //rip seconds and adjust ticks.
    toReturn = (ticks % (60.0 * divAmt)/divAmt) + toReturn; ticks = ((temp / (60.0 * divAmt)).toInt)
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
  def buildHTML_FromWindowWorker(item:WindowWorker):String={
    //get our most common name for this item.
    var name = item.getMostCommonTitle
    //trim if it's to long.
    if(name.length>guiNameLimit){name=name.substring(0,guiNameLimit)}
    //add the additional names if we are DEBUGGING!
    var additionalPayload:String = ""
    if(debug){ 
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
  def updateGUI():Unit={
    guiUpdateTicks %= guiUpdateEveryXTicks
    guiUpdateTicks += 1
    if(guiUpdateTicks != 1){
      return
    }
    //this is our init string for the label. gets the headers and table made.
    var guiHTMLGiantString = "<html><table border=\"1\" style=\"width:100%\">"+
      "<tr><th>Most Common Name</th><th>App Name</th><th>Time Open</th><th>Time Focused</th></tr>"
    //add items to our table
    for( index <- 0 until windowList.size) {
      guiHTMLGiantString=guiHTMLGiantString+buildHTML_FromWindowWorker(windowList.get(index))
    }
    //set the label to our table and close the table at the same time.
    guiLabel.setText(guiHTMLGiantString + "</table></html>")
  }
  def addWindowtoList(item:WindowWorker):Unit={
    //This could be made to exit as soon as found.
    for( index <- 0 until windowList.size) {
      //the current one we are testing, we should probably move this out of the loop to help with GC... probably wouldn't.
      val cur = windowList.get(index)
      //is this our golden item we are currently working on~!?
      if(cur.equals(item)){
        //this is our new item, update the old with the new
        cur.update(item)
        //we did find it~
        return
      }
    }
    //we didn't find the item, then we have a new window to add to our list~
    //finish contructing out item!
    item.buildIsNew
    //so add it.
    windowList.add(item)
  }
  def doLoop(time:Boolean):Unit={
    val loopStartTime = System.currentTimeMillis
    //System time passed since starting.
    realTicks = (System.currentTimeMillis-startupTime).toInt/sleepTime
    //How are we comparing?
    tickOffset = ticks - realTicks
    if(tickOffset > 0){return}
    //we're working, reflect the tick!
    ticks = ticks + 1
    //if debugging, add the current offset info, seems to hang out around 2MS after going for 30~40 seconds... stays there. FIX!
    var additionalPayload:String = ""
    if(debug){additionalPayload = " Off by: " + tickOffset + " Ticks."}
    //update our time label.
    guiTimeLabel.setText(guiTimeLabelString + buildTime(realTicks) + " from Ticks: "+ buildTime(ticks) + additionalPayload)
    //I would say to spawn threads for this but I don't think that would be the best of ways, it would lag more under load.
    val currentWindows:RawWindowLoading = new RawWindowLoading
    //these are the windows open right 'now'
    currentWindows.build
    //add it to the list for the proccessor to work on.
    windowsToProccess.put(currentWindows)
    //the getting windows can still take to long when under to much load.
    //maybe have another thread pool (10+?) that waits like the proccessor and all we do is add the new class into it and the threads build?
    val windowListTime = System.currentTimeMillis-loopStartTime
    //This will now print when we skip sleeps! (when under load?)
    if(time || (debug && !debug)){//turn this back on when messing with window.build
      println("Loop took "+(System.currentTimeMillis-loopStartTime)+"MS to complete! Window List Time: "+windowListTime)
    }
  }
  def shutDownHook()={
    //if we haven't already got someone working on this and if it isn't to soon to count, work this method.
    if(!shuttingDown){if(ticks > 10){
      //tell everyone else that we are handling this!
      shuttingDown = true
      //print out some additional data for our terminal lovers~
      println("Shutting down.")
      //wait for our proccessing que to finish
      while(!windowsToProccess.isEmpty){
        println("Waiting on proccessor.")
        Thread.sleep(sleepTime)
      }
      println("Clock = "+ sleepTime+", Ticks = "+ticks)
      //we should probably move this to the top list of vars/vals... second time we use it... but first is in INIT so twice in entire run...
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
      //make our new output file of what we found this time~
      val writer = new PrintWriter(new File("WW3K-"+format.format(new java.util.Date )+".log.csv"))
      //this is our header, in csv format... hopefully...
      val initString:String = "\"Window Title\",\"Window Owner\",\"Seconds Open\",\"Seconds Focused\",\"Additional Names[Ticks, Name]:\""
      //write to file
      writer.write(initString+System.lineSeparator)
      //write to terminal lovers~
      println(initString)
      //for each window we logged this session:
      for (index <- 0 until windowList.size) {
        //tell it to sort itself out (the window names... this probably isn't needed any more.)
        windowList.get(index).sort
        //and build the line to output.
        val line:String = "\"" + windowList.get(index).getMostCommonTitle() + "\",\"" +
          windowList.get(index).Owner + "\",\"" +
          windowList.get(index).counter / divAmt + "\",\"" +
          windowList.get(index).foCounter / divAmt+"\"," +
          windowList.get(index).getAllNames
        //write to file
        writer.write(line+System.lineSeparator)
        //write to terminal lovers~
        println(line)
      }
      //flush the file before closing
      writer.flush()
      //close the file.
      writer.close()
      //if the GUI is doing this shut down, then~
      if(guiShutDown){
        //ask if they want to shut down the whole computer, HANDLED BY OUTSIDE SCRIPT WRAPPER!
        val n:Int = JOptionPane.showConfirmDialog(null, "Shut Down whole computer?","Shut Down whole computer?",JOptionPane.YES_NO_OPTION);
        //if yes, Exit with 1, our sign to the script wrapper that we wanted to shut down.
        if (n == JOptionPane.YES_OPTION) {System.exit(1)
        } else if (n == JOptionPane.NO_OPTION) {//they said no, I have nothing to do
        } else {//they just closed the window on me :<
        }
      }
      //exit normally.
      System.exit(0)
    }}
  }
  //used to hold the results of a poll of the current windows and which is currently active.
  class RawWindowLoading{
    //our active window ID, init to our super special random number to know if it errored.
    var activeID:Int = -83
    //our list of windows, thianks to wmctrl, init to nothing because errors.
    var windows:String = ""
    def build()={
      try{
        //actually set our active window ID
        activeID = Integer.parseInt(("xdotool getactivewindow" !!).trim)
      }catch{
        //this happens often ish... might be when no window is focused, at which point it works perfectly and we can remove this println.
        case re:RuntimeException => println("Active Window Error: "+re.toString)
      }
      try{
        //try to get the list of windows.
        windows = ("wmctrl -lp" !!)//5~20, about as long as the first call above.
      }catch{
        //oh noes, there was an error, return no list because we have to return a list!
        case re:RuntimeException => println("Window List Error: "+re.toString)
      }
    }
  }
  def buildList(currentWindowSet:RawWindowLoading): Array[WindowWorker] = {
    //split each line (window data) into it's own thing
    var windowList: Array[String] = currentWindowSet.windows.split(System.lineSeparator)
    //this is the list of windows that are currently open... to be pulled from above array
    var WindowWorkers: Array[WindowWorker] = new Array[WindowWorker](windowList.length)
    //number of good, valid, windows.
    var good:Int = 0
    //for each of our listed windows:
    for (i <- 0 until windowList.length) {//200+MS!!
      //init to null, for knowing which one errored
      WindowWorkers(i) = null
      //init a current to the line from raw array
      val current:WindowWorker = new WindowWorker(windowList(i))
      try{
        //try to build it
        current.build //This now only builds a part of the window, otherwise it will take 200+MS instead of 0~1 MS!
        //check to see if we want this window
        if(current.wanted) {
          //we do, lets get it a home and check if it's the focused one~
          current.focused = currentWindowSet.activeID == current.ID
          //build worked, set it into the list
          WindowWorkers(i) = current
          //we have a good one
          good = good + 1
        }
      }catch{
        //this one was bac, tell it that it's broken so it can spell it's secret data.
        case re:RuntimeException => println("Error ["+windowList(i)+"]: "+re)
      }
    }
    //if we had bad window datas
    if(good < WindowWorkers.length){
      //make a new array of the correct size
      var temp:Array[WindowWorker] = new Array[WindowWorker](good)
      //location of new array
      var at:Int = 0
      //for each of our old array
      for( i <- 0 until WindowWorkers.length) {
        //if it's not null
        if(WindowWorkers(i)!=null){
          //add it to our new array
          temp(at) = WindowWorkers(i)
          //and move the index of our new array
          at = at + 1
        }
      }
      //set our fixed array as our old array.
      WindowWorkers = temp
    }
    return WindowWorkers
  }
  class WindowWorker(var base:String){
    //ID of this window, never changes unless window closes!
    var ID:Int = -1
    //not always present, so far, 'games' don't use correctly (Steam and Factorio...)
    var PID:Int = -1
    //check above about this value... meh... almost 3AM... bed time is midnight...
    var wanted:Boolean = true
    //the list of titles this window has gone by.
    var Titles:PriorityArrayList[String] = null//This is very heavy, a good 10MS per.
    //is this window currently focused? we can trash this after first update, it will only use foCounter... maybe use that instead?
    var focused:Boolean = false
    //the program we found using the PID from above
    var Owner:String = ""
    //how many times has this window been seen? well, we're INITing it, so atleast once!
    var counter:Int = 1
    //how many times have we seen this window focused on?
    var foCounter:Int = 0
    //sort our names
    def sort()=Titles.sort
    //get name X priority
    def getNamePriority(index:Int):Int=Titles.getPriority(index)
    //get priority and name X
    def getNameTotal(index:Int):String={Titles.getPriority(index)+":\""+Titles.get(index)+"\""}
    //get name X
    def getName(index:Int):String=Titles.get(index)
    //how many names have we seen so far?
    def getNameCount():Int=Titles.size
    //list all of our names in csv format, should clean up to not have quouts in names!
    def getAllNames():String={
      var toReturn:String = ""
      for( i <- 0 until Titles.size) {
        toReturn = toReturn + Titles.getPriority(i) + ",\"" + Titles.get(i) + "\","
      }
      return toReturn
    }
    //lowest should be most seen! get our most seen name
    def getMostCommonTitle():String=Titles.get(0)
    //I'm still open, pull my new data out of the baby.
    def update(newMe:WindowWorker)={
      //I was seen again >.>
      counter+=1
      //was I focused again?
      if(newMe.focused){
        //yes, I was seen mugging the users eyes~
        foCounter+=1
      }
      //what was my name this time?
      Titles.add(newMe.base)
    }
    def buildIsNew()={
      Titles = new PriorityArrayList[String]
      //we want duplicate names added to only increase the 'priority' of the name by one.
      Titles.duplicatesIncreasePriority = true
      //add it to our most common
      Titles.addRaw(base)
      //if this pid is actually useful
      if(PID!=0){
        //read the owner info
        Owner = (("ps -p " + PID + " -o comm=") !!)
        Owner = Owner.substring(0,Owner.length-1)
      }else{
        //otherwise, say that we don't know.
        //this should be unique enough, not many windows don't have a pid on my system... if it ever comes up, add more info to this.
        Owner = "!!No Proccess ID:"+ID+"!!"
      }
    }
    def buildName()={
      //this will be used to clean up notifications and other things, helping to merge more titles down.
      buildName_CleanNotifications
    }
    def buildName_CleanNotifications_Stub(open:Int, close:Int):Unit={
      //check to see if a trimmed string inside the open and close parens is a number, if so, rip open to close paren.
      try{
        //try to parse it, if parsed then it's a number!
        var noticon:Int = java.lang.Integer.parseInt(base.substring(open+1, close).trim)
        //it was a number, but lets use it for tracking purposes.
        noticon = 0
        //bit 0 = space after, bit 1 = space before. 0=none, 1=after, 2=before, 3=wrapped, no other values can be present!
        //bit 2 = closing is the end
        if(close < base.size-1){
          if(base.charAt(close+1)==' '){noticon += 1}
        }
        if(open>0){
          if(base.charAt(open-1)==' '){noticon += 2}
        }
        //this can be switched for a switch... don't remember scala switch though...
        if(noticon==0){
          //there is no spaces around the parens, return.
          if(base.length > close + 1){
            base = base.substring(0,open) + base.substring(close+1)
          }else{
            base = base.substring(0,open)
          }
        } else if(noticon==2){
          //only a space before, like a notifcation after the title
          if(base.length > close + 1){
            base = base.substring(0,open-1) + base.substring(close+1)
          }else{
            base = base.substring(0,open-1)
          }
        } else {//3 && 1
          //we're either wrapped with spaces or only have one at the end, either way, lets remove the last one... might cause issues...
          if(base.length > close + 2){
            base = base.substring(0,open) + base.substring(close+2)
          }else{
            base = base.substring(0,open)
          }
        }
      }catch{
        //expected error if not a number, just return because we have nothing to rip.
        case dontCare:NumberFormatException => return
        //DID NOT EXPECT THIS!
        case re:RuntimeException => println("Error: ["+base+"] -> "+re)
      }
    }
    def buildName_CleanNotifications()={
      //this method should now rip all numbers in parens out of the titles!
      //Still buggy!
      //"(3) (5)Tum(9)bl (2) rd(1) (8)" -> "(5)Tumbl rd(1)"
      //holds the name after each rip, used to rebuild after all rips.
      var base2:String = ""
      //the opening of the first paren
      var open:Int = base.indexOf("(")
      //the closing of the first paren
      var close:Int = base.indexOf(")")
      //while there is a closing.
      while(close>=0){
        //
        println("["+base+"]: ("+open+","+close+")")
        //if it's a valid closing, go to stub
        if(open>=0 && close>0 && close > open){
          buildName_CleanNotifications_Stub(open, close)
        }
        println("A")
        //move the begining part over to base2
        if(base.length>=close+1){
          base2 = base2 + base.substring(0, close+1)
        }
        println("B")
        //set the base to the rest
        if(base.length>=close+1){
          base = base.substring(close+1)
        }
        println("C")
        //update our new first paren group.
        open = base.indexOf("(")
        close = base.indexOf(")")
      }
      //rebuild our base after ripping.
      base = base2 + base
    }
    //build this window from the raw information
    def build():Unit={
      //our ID from hex
      ID = java.lang.Integer.parseInt(base.substring(2, base.indexOf(" ")), 16)
      base = base.substring(base.indexOf(" ")+1).trim
      //is this window useful to us?
      wanted = !base.substring(0,base.indexOf(" ")).contains("-")
      if(!wanted){return}
      base = base.substring(base.indexOf(" ")+1)
      //the pid registered with this window
      PID = java.lang.Integer.parseInt(base.substring(0,base.indexOf(" ")))
      base = base.substring(base.indexOf(" ")+1).trim
      //get the next space.
      val index  = base.indexOf(" ")
      //If there is no next space, this window has no window name!
      if(index < 0){base = "!!No Window Name:"+ID+"!!"}
      //index will be -1 if no space was found, else it will be >= 0.
      base = base.substring(index+1)
      //clean up the name.
      buildName
    }
    //Some type of magic happens here... not to sure but it does what's needed.
    def canEqual(a:Any)=a.isInstanceOf[WindowWorker]
    override def equals(that:Any):Boolean=that match{case that:WindowWorker=>that.canEqual(this)&&ID==that.ID&&PID==that.PID;case _=>false}
  }

  //This is a class I made to hold the names in an array list with additional numbers, higher numbers will bubble to the start. SORTABLE~
  class PriorityArrayList[T]{
    var arr:ArrayList[PriorityArrayListNode[T]] = new ArrayList[PriorityArrayListNode[T]]
    var duplicatesIncreasePriority:Boolean = false
    //
    def sort()=java.util.Collections.sort(arr)
    def trimToSize()=arr.trimToSize
    def size():Int=arr.size
    def set(index:Int, thing:T)=arr.set(index,new PriorityArrayListNode[T](thing))
    def remove(thing:T):Unit=if(contains(thing)){remove(indexOf(thing))}
    def remove(index:Int):T=arr.remove(index).get
    def lastIndexOf(thing:T):Int=arr.lastIndexOf(new PriorityArrayListNode[T](thing))
    def isEmpty():Boolean=arr.isEmpty
    def indexOf(thing:T):Int=arr.indexOf(new PriorityArrayListNode[T](thing))
    def setPriority(index:Int, value:Int)=arr.get(index).setPriority(value)
    def getPriority(index:Int):Int=arr.get(index).getPriority
    def get(index:Int):T=arr.get(index).get
    def contains(thing:T):Boolean=arr.contains(new PriorityArrayListNode[T](thing))
    def clear()=arr.clear
    def addRaw(thing:T):PriorityArrayListNode[T]={
      var item:PriorityArrayListNode[T] = new PriorityArrayListNode[T](thing)
      arr.add(item)
      return item
    }
    def add(thing:T):PriorityArrayListNode[T]={
      var item:PriorityArrayListNode[T] = new PriorityArrayListNode[T](thing)
      if(duplicatesIncreasePriority){
        var loc:Int = arr.indexOf(item)
        if(loc>=0){
          arr.get(loc).setPriority(1+arr.get(loc).getPriority)
          //sort method. from updated to begining or bigger priority.
          var done:Boolean = false
          while(loc>0&&(!done)){
            done = true
            if(arr.get(loc).isBefore(arr.get(loc-1))){
              arr.set(loc, arr.set(loc-1, arr.get(loc)));
              loc-=1
              done = false
            }
          }
          return arr.get(loc)
        }
      }
      if(arr.size > 0){
        for( i <- 0 until arr.size){
          if(item.isBefore(arr.get(i))){
            arr.add(i, item)
            return item
          }
        }
      }
      arr.add(item)
      return item
    }

    //this is actually what our array list is, wrap our data and include the number in the wrapping. these allow sortability.
    class PriorityArrayListNode[T](payload:T)extends Ordered[PriorityArrayListNode[T]]{
      var priority:Int = 1
      def getPriority():Int={return priority}
      def setPriority(value:Int)={priority = value}
      def get():T={return payload}
      def isBefore(other:PriorityArrayListNode[T]):Boolean=compare(other) <= 0
      def isAfter(other:PriorityArrayListNode[T]):Boolean=compare(other) > 0
      //if other is higher than it goes before this one... funked me up at first...
      override def compare(other:PriorityArrayListNode[T]):Int={other.priority-priority}
      override def toString():String={return "["+priority+":"+payload.toString+"]"}
      //Some type of magic happens here... not to sure but it does what's needed.
      def canEqual(a:Any)=a.isInstanceOf[PriorityArrayListNode[T]]
      override def equals(that:Any):Boolean=that match{
        case that:PriorityArrayListNode[T]=>that.canEqual(this)&&payload.equals(that.get)
        case _=>false
      }
    }
  }
}

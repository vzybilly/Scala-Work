//Made by vzybilly.
//WindowWatcher3K

import java.io.PrintWriter
import java.io.File
import javax.swing.JOptionPane
import sys.process._ //this is good as is, I think all are used anyway.

class WW3K_Logic(varls:WW3K_Varls){
  //This is the main Method of the program!
  def mainLoop()={
    //add our shutdown hook
    //I think this broke along the way, don't care, use close button to make sure!
    Runtime.getRuntime.addShutdownHook(new Thread{override def run{shutDownHook}})
    //later, build the GUI...
    varls.gui.build
    //start up our proccessor
    varls.windowsProccessor.start
    //when did we start?
    varls.startupTime = System.currentTimeMillis
    //we never want to stop!... unless shutting down
    while(!varls.shuttingDown){
      //do our loop.
      doLoop()
      //sleep.
      Thread.sleep(varls.sleepTime/10)
    }
  }
  //tick.
  def doLoop():Unit={
    val loopStartTime = System.currentTimeMillis
    //System time passed since starting.
    varls.realTicks = (System.currentTimeMillis-varls.startupTime).toInt/varls.sleepTime
    //How are we comparing?
    varls.tickOffset = varls.ticks - varls.realTicks
    if(varls.tickOffset > 0){return}
    //we're working, reflect the tick!
    varls.ticks += 1
    //Tell the GUI to update the Time!
    varls.gui.updateTime
    //How many ticks are we late? assume 0.
    var tickDiff:Int = 0
    if(varls.tickOffset < 0){
      //how many ticks have we skipped accidentally?
      tickDiff = varls.tickOffset * -1
      //adjust ticks to show we 'counted' them
      varls.ticks += tickDiff
      //reupdate tick offset
      varls.tickOffset = varls.ticks - varls.realTicks
    }
    //I would say to spawn threads for this but I don't think that would be the best of ways, it would lag more under load.
    val currentWindows:WW3K_RawWindowLoading = new WW3K_RawWindowLoading(tickDiff, varls)
    //add it to the list for the proccessor to work on.
    varls.windowsToProccess.put(currentWindows)
    //the getting windows can still take to long when under to much load.
    val windowListTime = System.currentTimeMillis-loopStartTime
    //This will now print when we skip sleeps! (when under load?)
    if(varls.debug && !varls.debug){//turn this back on when messing with window.build
      println("Loop took "+(System.currentTimeMillis-loopStartTime)+"MS to complete! Window List Time: "+windowListTime)
    }
  }
  //Get the window list.
  def getRawWindows():String={
    try{
      //try to get the list of windows.
      return ("wmctrl -lp" !!)//5~20, about as long as the first call above.
    }catch{
      //oh noes, there was an error, return no list because we have to return a list!
      case re:RuntimeException => return ""//println("Window List Error: "+re.toString)
    }
  }
  //Get the active Window ID
  def getActiveWindow():Int={
    try{
      //actually set our active window ID
      return Integer.parseInt(("xdotool getactivewindow" !!).trim)
    }catch{
      //this happens often ish... might be when no window is focused, at which point it works perfectly and we can remove this println.
      case re:RuntimeException => return -83//println("Active Window Error: "+re.toString)
    }
  }
  //Get the name of the proccess owning the Window!
  def getProccessNameFromPid(pid:Int):String={
    return (("ps -p " + pid + " -o comm=") !!)
  }
  //Build the windows from the raw data.
  def buildList(currentWindowSet:WW3K_RawWindowLoading):Array[WW3K_Window]={
    //split each line (window data) into it's own thing
    var windowList: Array[String] = currentWindowSet.windows.split(System.lineSeparator)
    //this is the list of windows that are currently open... to be pulled from above array
    var WindowWorkers: Array[WW3K_Window] = new Array[WW3K_Window](windowList.length)
    //number of good, valid, windows.
    var good:Int = 0
    //for each of our listed windows:
    for (i <- 0 until windowList.length) {
      //init to null, for knowing which one errored
      WindowWorkers(i) = null
      //init a current to the line from raw array
      val current:WW3K_Window = new WW3K_Window(windowList(i), currentWindowSet.tickDiff, varls)
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
        case re:RuntimeException => println("Error(Build) ["+windowList(i)+"]: "+re)
      }
    }
    //if we had bad window datas
    if(good < WindowWorkers.length){
      //make a new array of the correct size
      var temp:Array[WW3K_Window] = new Array[WW3K_Window](good)
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
  //Add the window to the window list.
  def addWindowtoList(item:WW3K_Window):Unit={
    //This could be made to exit as soon as found.
    for( index <- 0 until varls.windowList.size) {
      //the current one we are testing, we should probably move this out of the loop to help with GC... probably wouldn't.
      val cur:WW3K_Window = varls.windowList.get(index)
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
    varls.windowList.add(item)
  }
  //shut down the program.
  def shutDownHook()={
    //if we haven't already got someone working on this and if it isn't to soon to count, work this method.
    if(!varls.shuttingDown){if(varls.ticks > 10){
      //tell everyone else that we are handling this!
      varls.shuttingDown = true
      //print out some additional data for our terminal lovers~
      println("Shutting down.")
      //wait for our proccessing que to finish
      while(varls.proccessedTicks < varls.ticks){
        println("  Waiting on proccessor. que count: "+varls.windowsToProccess.size)
        Thread.sleep(varls.sleepTime)
      }
      println("Clock = "+ varls.sleepTime+", Ticks = "+varls.ticks)
      //we should probably move this to the top list of vars/vals... second time we use it... but first is in INIT so twice in entire run...
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
      //make our new output file of what we found this time~
      val writer = new PrintWriter(new File("WW3K-"+format.format(new java.util.Date )+".log.csv"))
      //this is our header, in csv format... hopefully...
      val initString:String = "\"Window Title\",\"Window Owner\",\"Seconds Open\",\"Seconds Focused\",\"WID\",\"PID\",\"Additional Names[Ticks, Name]:\""
      //write to file
      writer.write(initString+System.lineSeparator)
      //write to terminal lovers~
      println(initString)
      //for each window we logged this session:
      for (index <- 0 until varls.windowList.size) {
        //tell it to sort itself out (the window names... this probably isn't needed any more.)
        val cur:WW3K_Window = varls.windowList.get(index)
        cur.sort
        //and build the line to output.
        val line:String = "\"" + cur.getMostCommonTitle() + "\",\"" +
          cur.Owner + "\",\"" +
          cur.counter / varls.divAmt + "\",\"" +
          cur.foCounter / varls.divAmt + "\",\"" +
          cur.ID + "\",\"" +
          cur.PID + "\"," +
          cur.getAllNames
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
      if(varls.gui.shutDown){
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
}
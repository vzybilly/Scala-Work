//Made by vzybilly.
//WindowWatcher3K

import java.util.ArrayList
import java.util.concurrent.LinkedBlockingQueue

class WW3K_Varls{
  //Our logical Classes
  //Class for all the core logic.
  val logic:WW3K_Logic = new WW3K_Logic(this)
  //Class for all the GUI logic
  val gui:WW3K_GUI = new WW3K_GUI(this)
  //Secondary thread Class used to proccess the windows and update the GUI!
  val windowsProccessor:WW3K_ProccessingThread = new WW3K_ProccessingThread(this)

  //Data to share
  //the list of windows this session.
  val windowList:ArrayList[WW3K_Window] = new ArrayList[WW3K_Window]
  //the amount of MS to sleep
  //Load from file!
  val sleepTime:Int = 100
  //used to pull seconds from ticks. (how many ticks in a second? this many.)
  var divAmt:Double = 1000.0 / sleepTime;
  //Are we actively handling a shutdown or have already handled?
  var shuttingDown:Boolean = false
  //what was the system MS when we started?
  var startupTime:Long = -1
  //How long as the pgram been running using system time?
  var realTicks:Int = 0
  //how many runs have we had since starting?
  var ticks:Int = 0
  //How many ticks are we off by? negetive is behind!
  var tickOffset:Int = 0
  //How many ticks have we completed?
  var proccessedTicks:Int = 0
  //Are we debugging the program? (Has a GUI button to toggle!)
  var debug:Boolean = false
  var debugTickOffset:Boolean = false
  var debugQueSize:Boolean = false
  var debugWID:Boolean = false
  var debugPID:Boolean = false
  var debugOtherNames:Boolean = false
  //Used to pass window info from our main loop to our secondary thread!
  val windowsToProccess:LinkedBlockingQueue[WW3K_RawWindowLoading] = new LinkedBlockingQueue[WW3K_RawWindowLoading](42000)
}
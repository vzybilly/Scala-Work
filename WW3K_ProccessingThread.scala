//Made by vzybilly.
//WindowWatcher3K

class WW3K_ProccessingThread(varls:WW3K_Varls) extends Thread{
  override def run{
    var lastUsed:Int = -1
    var current:WW3K_RawWindowLoading = null
    var windows:Array[WW3K_Window] = null
    //we want this to run if we are not shutting down or while the que has items.
    while(!varls.shuttingDown || !varls.windowsToProccess.isEmpty){
      //windowsToProccess.take will wait till there in one to take!
      try{
        current = varls.windowsToProccess.take//This now throws an exception "InterruptedException" put all of this loop body into a try!
      }catch{
        case re:InterruptedException => println("!Proccessor was interrupted!"); current = null
      }
      //reset our counter for last used windows
      lastUsed = 0
      //Go through the current window list and tell all of them that they are not the most recent.
      for(index <- 0 until varls.windowList.size){
        //get the count of the last used windows
        if(varls.windowList.get(index).usedLast){
          lastUsed += 1
        }
        //tell the current one that it is not the most recent.
        varls.windowList.get(index).usedLast = false
      }
      //Build the window data from the current raw data.
      if(current == null){
        windows = new Array[WW3K_Window](0)
      }else{
        windows = varls.logic.buildList(current)
      }
      //for each of our new windows, if we have it, update the old with the new, else, add it.
      for(item:WW3K_Window <- windows){
        varls.logic.addWindowtoList(item)
      }
      //If we have a new or lost a window, we need to sort our list.
      if(lastUsed != windows.length){
        //we have to sort our list now.
        java.util.Collections.sort(varls.windowList)
      }
      //now that we did all the work with the new list of windows, update the GUI to reflect our perfection~<3
      varls.gui.update
      //If we still have more...
      if(!varls.windowsToProccess.isEmpty){
        //Give the CPU a break
        Thread.sleep(5)
      }
      //we proccess a this amount of ticks.
      if(current!=null){
        //We didn't actually proccess anything if current==null!
        varls.proccessedTicks += current.tickDiff + 1
      }
    }
  }
}
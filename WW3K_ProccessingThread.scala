//Made by vzybilly.
//WindowWatcher3K

class WW3K_ProccessingThread(varls:WW3K_Varls) extends Thread{
  override def run{
    //we want this to run if we are not shutting down or while the que has items.
    while(!varls.shuttingDown || !varls.windowsToProccess.isEmpty){
      //windowsToProccess.take will wait till there in one to take!
      val current = varls.windowsToProccess.take
      //Go through the current window list and tell all of them that they are not the most recent.
      for(index <- 0 until varls.windowList.size){
        //tell the current one that it is not the most recent.
        varls.windowList.get(index).usedLast = false
      }
      //Build the window data from the current raw data.
      var windows:Array[WW3K_Window] = varls.logic.buildList(current)
      //for each of our new windows, if we have it, update the old with the new, else, add it.
      for(item:WW3K_Window <- windows){
        varls.logic.addWindowtoList(item)
      }
      //now that we did all the work with the new list of windows, update the GUI to reflect our perfection~<3
      varls.gui.update
      //If we still have more...
      if(!varls.windowsToProccess.isEmpty){
        //Give the CPU a break
        Thread.sleep(5)
      }
      //we proccess a this amount of ticks.
      varls.proccessedTicks += current.tickDiff + 1
    }
  }
}
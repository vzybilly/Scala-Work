//Made by vzybilly.
//WindowWatcher3K

class WW3K_ProccessingThread(varls:WW3K_Varls) extends Thread{
  override def run{
    //we want this to run if we are not shutting down or while the que has items.
    while(!varls.shuttingDown || !varls.windowsToProccess.isEmpty){
      //windowsToProccess.take will wait till there in one to take!
      val current = varls.windowsToProccess.take
      var windows:Array[WW3K_Window] = varls.logic.buildList(current)
      /*
        !Add abit of work here to bubble up the still open windows,
        !just add another flag to them and reset all set flags and while updating set them.
        !new windows should have the flag set automatically.
        !once done, bubble them up.
        !I don't expect many to close instantly so a simple bubble should work...
        !maybe after the add, move up to the last one that's set or first...
        !should work like that and probably won't be to cost heavy.
        !You can have the unchecker end at the first non-checked item, that should work!
        !If it doesn't, when it adds, it should bubble up past anyway but this could use some checking!
      // */
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
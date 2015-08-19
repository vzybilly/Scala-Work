//Made by vzybilly.
//WindowWatcher3K

//used to hold the results of a poll of the current windows and which is currently active.
class WW3K_RawWindowLoading(val tickDiff:Int, varls:WW3K_Varls){
  //our active window ID, init to our super special random number to know if it errored.
  var activeID:Int = varls.logic.getActiveWindow
  //our list of windows, thianks to wmctrl, init to nothing because errors.
  var windows:String = varls.logic.getRawWindows
}
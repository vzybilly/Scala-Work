//Made by vzybilly.
//WindowWatcher3K

import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities

//Used to work the Debug GUI, to set and unset the debug options.
class WW3K_Debug_GUI(varls:WW3K_Varls){
  //we are to close our GUIs.
  def close()={
    //we just stole this from the GUI class, don't tell anyone.
    if(frame != null){
      SwingUtilities.invokeLater(
        new Runnable{
          def run(){
            frame.dispose
          }
        }
      )
    }
  }
  //we are to build our GUIs.
  def build()={
    if(frame == null){
      frame = buildWindow
    }
  }
  //we are to toggle the visibility of our GUIs.
  def toggle()={
    if(frame != null){
      frame.setVisible(!frame.isVisible)
    }
  }
  //our main GUI window
  var frame:JFrame = null
  def buildWindow():JFrame={
    //build the window
    val win:JFrame = new JFrame("WW3K - Debug Options");
    win.add(buildPanel)
    win.pack
    win.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)
    win.setVisible(false)
    return win
  }
  def buildPanel:JPanel={
    //build the panel of the window.
    return null
  }
}
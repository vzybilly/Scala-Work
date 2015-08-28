//Made by vzybilly.
//WindowWatcher3K

import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
import java.awt.BorderLayout

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
  //build a action listener that calls a function we pass to this.
  def actionListener(f: =>Unit)=new java.awt.event.ActionListener{//Stolen from GUI class.
    def actionPerformed(e:java.awt.event.ActionEvent){
      f
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
  var debugBtn:JButton = null
  def buildPanel:JPanel={
    //build the panel of the window.
    val panel:JPanel = new JPanel
    panel.setLayout(new BorderLayout )
    //build the button on this panel
    debugBtn = new JButton("Global Debug: " + varls.debug)
    debugBtn.addActionListener(actionListener(btnToggleMain))
    //Main
    panel.add(buildMainPanel, BorderLayout.CENTER)
    //Bottom
    panel.add(debugBtn, BorderLayout.PAGE_END)
    return panel
  }
  def buildMainPanel:JPanel={
    //build the panel of the window.
    val panel:JPanel = new JPanel
    return panel
  }
  //button methods.
  def btnToggleMain()={
    varls.debug = !varls.debug
    debugBtn.setText("Global Debug: " + varls.debug)
  }
}
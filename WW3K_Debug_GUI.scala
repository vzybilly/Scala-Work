//Made by vzybilly.
//WindowWatcher3K

import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import javax.swing.BoxLayout
import java.util.ArrayList

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
    //top to bottom layout.
    val layout:BoxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.setLayout(layout)
    buildButtons
    for(btn:JButton <- btnArray){
      panel.add(btn)
    }
    //do the things.
    return panel
  }
  var btnArray:Array[JButton] = null
  def buildButtons={
    var arr:ArrayList[JButton] = new ArrayList[JButton]
    arr.add(new JButton("Tick Offset: " + varls.debugTickOffset))
    arr.get(arr.size-1).addActionListener(actionListener(btnToggleTickOffset))
    arr.add(new JButton("Que Size: " + varls.debugQueSize))
    arr.get(arr.size-1).addActionListener(actionListener(btnToggleQueSize))
    arr.add(new JButton("Window ID: " + varls.debugWID))
    arr.get(arr.size-1).addActionListener(actionListener(btnToggleWID))
    arr.add(new JButton("Proccess ID: " + varls.debugPID))
    arr.get(arr.size-1).addActionListener(actionListener(btnTogglePID))
    arr.add(new JButton("Additional Window Names: " + varls.debugOtherNames))
    arr.get(arr.size-1).addActionListener(actionListener(btnToggleOtherNames))
    btnArray = arr.toArray(new Array[JButton](arr.size))
  }
  //button methods.
  def btnToggleTickOffset={
    varls.debugTickOffset = !varls.debugTickOffset
    btnArray(0).setText("Tick Offset: "+varls.debugTickOffset)
  }
  //button methods.
  def btnToggleQueSize={
    varls.debugQueSize = !varls.debugQueSize
    btnArray(1).setText("Que Size: "+varls.debugQueSize)
  }
  //button methods.
  def btnToggleWID={
    varls.debugWID = !varls.debugWID
    btnArray(2).setText("Window ID: "+varls.debugWID)
  }
  //button methods.
  def btnTogglePID={
    varls.debugPID = !varls.debugPID
    btnArray(3).setText("Proccess ID: "+varls.debugPID)
  }
  //button methods.
  def btnToggleOtherNames={
    varls.debugOtherNames = !varls.debugOtherNames
    btnArray(4).setText("Additional Window Names: "+varls.debugOtherNames)
  }
  def btnToggleMain()={
    varls.debug = !varls.debug
    debugBtn.setText("Global Debug: " + varls.debug)
  }
}
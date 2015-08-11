
import sys.process._
import javax.swing.JOptionPane

object Tester{def main(args: Array[String])={
    //Please use this indent.

    //Test out http://docs.oracle.com/javase/7/docs/api/javax/swing/JTextPane.html
    //It might have some fun with making HTML windows in java for my entire program GUI!
    println("running")
    println((("./waitPrint.sh") !))
    val n:Int = JOptionPane.showConfirmDialog(
      null /*frame*/, "Shut Down whole computer?",
      "Shut Down whole computer?",
      JOptionPane.YES_NO_OPTION);
    if (n == JOptionPane.YES_OPTION) {
      println("Ewww!");
    } else if (n == JOptionPane.NO_OPTION) {
      println("Me neither!");
    } else {
      println("Come on -- tell me!");
    }
  }
}
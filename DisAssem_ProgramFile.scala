//Made by vzybilly

import java.io.File
import java.util.ArrayList

class DisAssem_ProgramFile(file:File){
  val validFile = validateFile()
  //This can not handle files bigger than ~2GB
  val memory = if (validFile) new Array[Byte](file.length.toInt) else null
  if (memory != null) {
    loadFile
  }
  def get(address:Long):Int={//gets the unsigned byte.
    return memory(address.toInt).toInt & 0xFF
  }
  //Construction Methods
  def loadFile():Unit={
    var at = -1L
    val in = new java.io.FileInputStream(file)
    var data = -1
    while(at < file.length){
      while(in.available>0){
        data = in.read
        at = at + 1
        if(data == -1){
          return
        } else {
          memory(at.toInt)=data.toByte
        }
      }
      Thread.sleep(1)
    }
  }
  def validateFile():Boolean={
    if(file.isFile){//Exists? -> File? -> true.
      if(file.canRead){//Exists? -> can Read? -> true.
        return true
      }
    }
    return false
  }
}
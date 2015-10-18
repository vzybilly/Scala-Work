
import java.io.BufferedInputStream
import java.io.File

class DisAssem_ELF{
  // https://en.wikipedia.org/wiki/Executable_and_Linkable_Format
  def buildHeader(in:BufferedInputStream):ELF_Header={}
  def isValid(file:File):Boolean={
    val in:BufferedInputStream = new BufferedInputStream(new java.io.FileInputStream(file))
    //Magic Number Test
    if(in.read != 0x7F){
      return false
    }
    if(in.read != 0x45){
      return false
    }
    if(in.read != 0x4C){
      return false
    }
    if(in.read != 0x46){
      return false
    }
    return true
  }
  class ELF_Header{
    //0x00 - 4 is Magic Number.
    //0x04 - 1
    var class = -1
    //0x05 - 1
    var data = -1
    //0x06 - 1
    var version = -1
    //0x07 - 1
    var osABI = -1
    //0x08 - 1
    var abiVersion = -1
    //0x09 - 7, padding
    //Begin funky loading
    //0x10 - 2
    var type = -1
    //0x12 - 2
    var machine = -1
    //0x14 - 4
    var elfVersion = -1
    //0x18 - 4[32],8[64]
    var programEntryPoint = -1
  }
}
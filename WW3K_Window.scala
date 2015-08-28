//Made by vzybilly.
//WindowWatcher3K

class WW3K_Window(var base:String, tickDiff:Int, varls:WW3K_Varls)extends Ordered[WW3K_Window]{
  //ID of this window, never changes unless window closes!
  var ID:Int = -1
  //not always present, so far, 'games' don't use correctly (Steam and Factorio...)
  var PID:Int = -1
  //check above about this value... meh... almost 3AM... bed time is midnight...
  var wanted:Boolean = true
  //the list of titles this window has gone by.
  var titles:PriorityArrayList[String] = null//This is very heavy, a good 10MS per.
  //is this window currently focused? we can trash this after first update, it will only use foCounter... maybe use that instead?
  var focused:Boolean = false
  //the program we found using the PID from above
  var Owner:String = ""
  //how many times has this window been seen? well, we're INITing it, so atleast once!
  var counter:Int = 1 + tickDiff
  //how many times have we seen this window focused on?
  var foCounter:Int = 1 + tickDiff
  //Have we been used since the last time?
  var usedLast:Boolean = true
  //Used to sort us on the main list. Sorts by: Last used [yes, no], counter [big, small], focusCount [big, small]
  override def compare(other:WW3K_Window):Int={
    //negetive is us first
    //positive is them first
    if(usedLast == other.usedLast){
      if(counter!=other.counter){
        return other.counter - counter
      }else{
        return other.foCounter - foCounter
      }
    }
    //one of us were used last and the other wasn't!
    if(usedLast){//I was.
      return -1
    }
    //they were.
    return 1
  }
  //sort our names
  def sort()=titles.sort
  //get name X priority
  def getNamePriority(index:Int):Int=titles.getPriority(index)
  //get priority and name X
  def getNameTotal(index:Int):String={titles.getPriority(index)+":\""+titles.get(index)+"\""}
  //get name X
  def getName(index:Int):String=titles.get(index)
  //how many names have we seen so far?
  def getNameCount():Int=titles.size
  //list all of our names in csv format, should clean up to not have quouts in names!
  def getAllNames():String={
    var toReturn:String = ""
    for( i <- 0 until titles.size) {
      toReturn = toReturn + titles.getPriority(i) + ",\"" + titles.get(i) + "\","
    }
    return toReturn
  }
  //lowest should be most seen! get our most seen name
  def getMostCommonTitle():String=titles.get(0)
  //I'm still open, pull my new data out of the baby.
  def update(newMe:WW3K_Window)={
    //I was seen again >.>
    counter+=newMe.counter
    //was I focused again?
    if(newMe.focused){
      //yes, I was seen mugging the users eyes~
      foCounter+=newMe.foCounter
    }
    //what was my name this time?
    titles.add(newMe.base)
    //we were used!
    usedLast = true
  }
  def buildIsNew()={
    titles = new PriorityArrayList[String]
    //we want duplicate names added to only increase the 'priority' of the name by one.
    titles.duplicatesIncreasePriority = true
    //add it to our most common
    titles.addRaw(base)
    //if this pid is actually useful
    if(PID!=0){
      //read the owner info
      Owner = varls.logic.getProccessNameFromPid(PID)
      Owner = Owner.substring(0,Owner.length-1)
    }else{
      //otherwise, say that we don't know.
      //this should be unique enough, not many windows don't have a pid on my system... if it ever comes up, add more info to this.
      Owner = "!No Proccess ID:"+ID+"!"
    }
  }
  def buildName()={
    //this will be used to clean up notifications and other things, helping to merge more titles down.
    buildName_CleanNotifications
  }
  def buildName_CleanNotifications_Stub(open:Int, close:Int):Int={
    //check to see if a trimmed string inside the open and close parens is a number, if so, rip open to close paren.
    try{
      var toReturn:Int = 0
      //try to parse it, if parsed then it's a number!
      var noticon:Int = java.lang.Integer.parseInt(base.substring(open+1, close).trim)
      //it was a number, but lets use it for tracking purposes.
      noticon = 0
      //bit 0 = space after, bit 1 = space before. 0=none, 1=after, 2=before, 3=wrapped, no other values can be present!
      //bit 2 = closing is the end
      if(close < base.size-1){
        if(base.charAt(close+1)==' '){noticon += 1}
      }
      if(open>0){
        if(base.charAt(open-1)==' '){noticon += 2}
      }
      //this can be switched for a switch... don't remember scala switch though...
      if(noticon==0){
        //there is no spaces around the parens, return.
        if(base.length > close + 1){
          toReturn = ((close+1)-open)
          base = base.substring(0,open) + base.substring(close+1)
        }else{
          toReturn = base.length - open
          base = base.substring(0,open)
        }
      } else if(noticon==2){
        //only a space before, like a notifcation after the title
        if(base.length > close + 1){
          toReturn = ((close+1)-(open-1))
          base = base.substring(0,open-1) + base.substring(close+1)
        }else{
          toReturn = base.length - (open - 1)
          base = base.substring(0,open-1)
        }
        toReturn *= -1
      } else {//3 && 1
        //we're either wrapped with spaces or only have one at the end, either way, lets remove the last one... might cause issues...
        if(base.length > close + 2){
          toReturn = ((close+2)-open)
          base = base.substring(0,open) + base.substring(close+2)
        }else{
          toReturn = base.length - open
          base = base.substring(0,open)
        }
      }
      //tell the lovely people how much we took from the string.
      return toReturn
    }catch{
      //expected error if not a number, just return because we have nothing to rip.
      case dontCare:NumberFormatException => return 0
      //DID NOT EXPECT THIS! We didn't(?) rip anything!
      case re:RuntimeException => println("Error(Noti): ["+base+"] -> "+re); return 0
    }
  }
  def buildName_CleanNotifications()={
    //this method should now rip all numbers in parens out of the titles!
    //holds the name after each rip, used to rebuild after all rips.
    var base2:String = ""
    //the opening of the first paren
    var open:Int = base.indexOf("(")
    //the closing of the first paren
    var close:Int = base.indexOf(")")
    //while there is a closing.
    while(close>=0){
      //how much did we rip out of the title on this pass?
      var ripAmount:Int = 0
      //if we actually have an opening paren.
      if(open>=0){
        //if it's a valid closing, go to stub
        if(close>0 && close > open){
          ripAmount = buildName_CleanNotifications_Stub(open, close)
        }
        //If rip amount is negetive, we removed one before open, 0 is for errors and non numbers alike.
        if(ripAmount < 0){
          //shift our open to know... we don't actually use this if we've ripped, which we clearly did... clean up work!
          open -= 1
          //fix up our rip amount.
          ripAmount *= -1
        }
        //If we did not rip anything, then we have to move it to another string, otherwise it'll infi loop.
        if(ripAmount==0){
          //move the begining part over to base2
          base2 = base2 + base.substring(0, close + 1)
          //set the base to the rest
          if(base.length >= close + 1){
            base = base.substring(close + 1)
          }else{
            base = ""
          }
        }
      }else{//We couldn't actually do anything, go ahead and move the first bit off.
        // we could probably remove this and move the open>=0 back to the Stub If.
        base2 = base2 + base.substring(0,close+1)
        if(base.length>close+1){
          base = base.substring(close+1)
        }else{
          base = ""
        }
      }
      //update our new first paren group.
      open = base.indexOf("(")
      close = base.indexOf(")")
    }
    //rebuild our base after ripping.
    base = base2 + base
  }
  //build this window from the raw information
  def build():Unit={
    //our ID from hex
    ID = java.lang.Integer.parseInt(base.substring(2, base.indexOf(" ")), 16)
    base = base.substring(base.indexOf(" ")+1).trim
    //is this window useful to us?
    wanted = !base.substring(0,base.indexOf(" ")).contains("-")
    if(!wanted){return}
    base = base.substring(base.indexOf(" ")+1)
    //the pid registered with this window
    PID = java.lang.Integer.parseInt(base.substring(0,base.indexOf(" ")))
    base = base.substring(base.indexOf(" ")+1).trim
    //get the next space.
    val index  = base.indexOf(" ")
    //If there is no next space, this window has no window name!
    if(index < 0){base = "!No Window Name:"+ID+"!"}
    //index will be -1 if no space was found, else it will be >= 0.
    base = base.substring(index+1)
    //clean up the name.
    buildName
  }
  //Some type of magic happens here... not to sure but it does what's needed.
  def canEqual(a:Any)=a.isInstanceOf[WW3K_Window]
  override def equals(that:Any):Boolean=that match{case that:WW3K_Window=>that.canEqual(this)&&ID==that.ID&&PID==that.PID;case _=>false}
}
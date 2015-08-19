//Made by vzybilly.
//WindowWatcher3K

import java.util.ArrayList

//ArrayList with numbers used to sort it actively, It goes from largest to smallest.
class PriorityArrayList[T](stockPriority:Int=1){
  //the ArrayList used to hold our wrapping class to have Prioritys!
  var arr:ArrayList[PriorityArrayListNode[T]] = new ArrayList[PriorityArrayListNode[T]]
  //if a equals() is true, and this is true, add one to the priority.
  var duplicatesIncreasePriority:Boolean = false
  //Resort the priorities, incase things get funky.
  def sort()=java.util.Collections.sort(arr)
  //ArrayList Method.
  def trimToSize()=arr.trimToSize
  //ArrayList Method.
  def size():Int=arr.size
  //ArrayList Method. Priority = 1
  def set(index:Int, thing:T)=arr.set(index,new PriorityArrayListNode[T](thing))
  //ArrayList Method.
  def remove(thing:T):Unit=if(contains(thing)){remove(indexOf(thing))}
  //ArrayList Method.
  def remove(index:Int):T=arr.remove(index).get
  //ArrayList Method.
  def lastIndexOf(thing:T):Int=arr.lastIndexOf(new PriorityArrayListNode[T](thing))
  //ArrayList Method.
  def isEmpty():Boolean=arr.isEmpty
  //ArrayList Method.
  def indexOf(thing:T):Int=arr.indexOf(new PriorityArrayListNode[T](thing))
  //Bubble from location.
  def bubble(location:Int)={
    //sort method. from updated to begining or bigger priority.
    //are we done with the bubble?
    var done:Boolean = false
    var at:Int = location
    //while we're not at the beginning and not done.
    while(at > 0 && (!done)){
      //let's just say this is the last one.
      done = true
      //check if we can move up
      if(arr.get(at).isBefore(arr.get(at - 1))){//we can
        //nifty swap.
        arr.set(at, arr.set(at - 1, arr.get(at)));
        //we moved one up.
        at -= 1
        //we moved so we're not done.
        done = false
      }
    }
  }
  //Sets the priority of selected Item
  def setPriority(index:Int, value:Int)={
    arr.get(index).setPriority(value)
    //Bubble the list.
    bubble(index)
  }
  //Gets the priority of selected Item
  def getPriority(index:Int):Int=arr.get(index).getPriority
  //ArrayList Method.
  def get(index:Int):T=arr.get(index).get
  //ArrayList Method.
  def contains(thing:T):Boolean=arr.contains(new PriorityArrayListNode[T](thing))
  //ArrayList Method.
  def clear()=arr.clear
  //Don't check if it is already there, just directly add it. Built to skip duplicatesIncreasePriority and add Directly to the end!
  def addRaw(thing:T):PriorityArrayListNode[T]={
    var item:PriorityArrayListNode[T] = new PriorityArrayListNode[T](thing)
    //Directly add the item.
    arr.add(item)
    //Return our special item.
    return item
  }
  def add(thing:T):PriorityArrayListNode[T]={
    var item:PriorityArrayListNode[T] = new PriorityArrayListNode[T](thing)
    //Are we checking for duplicates?
    if(duplicatesIncreasePriority){//If we have it already, increment priority.
      //the location of the item, -1 if we don't have it.
      var location:Int = arr.indexOf(item)
      //is it found~?
      if(location>=0){
        //increment the priority
        arr.get(location).setPriority(1+arr.get(location).getPriority)
        //Bubble the list.
        bubble(location)
        //Return our special item.
        return arr.get(location)
      }
      //if it's not found, duplicates don't matter so we just add it.
    }
    //if we have items, go through them...
    for( i <- 0 until arr.size){
      //if our new item is before the current item...
      if(item.isBefore(arr.get(i))){
        //then add it before the item.
        arr.add(i, item)
        //Return our special item.
        return item
      }
    }
    //Add the item to the end of the list.
    arr.add(item)
    //Return our special item.
    return item
  }

  //this is actually what our array list is, wrap our data and include the number in the wrapping. these allow sortability.
  class PriorityArrayListNode[T](payload:T)extends Ordered[PriorityArrayListNode[T]]{
    //set our priority to our stock priority.
    var priority:Int = stockPriority
    //Get our Priority
    def getPriority():Int={return priority}
    //Set our Priority
    def setPriority(value:Int)={priority = value}
    //Get our payload, the actual data they wanted to store.
    def get():T={return payload}
    //Are we before the other?
    def isBefore(other:PriorityArrayListNode[T]):Boolean=compare(other) <= 0
    //Are we after the other?
    def isAfter(other:PriorityArrayListNode[T]):Boolean=compare(other) > 0
    //if other is higher than it goes before this one... funked me up at first...
    override def compare(other:PriorityArrayListNode[T]):Int={other.priority-priority}
    //derp.
    override def toString():String={return "["+priority+":"+payload.toString+"]"}
    //Some type of magic happens here... not to sure but it does what's needed.
    def canEqual(a:Any)=a.isInstanceOf[PriorityArrayListNode[T]]
    override def equals(that:Any):Boolean=that match{
      case that:PriorityArrayListNode[T]=>that.canEqual(this)&&payload.equals(that.get)
      case _=>false
    }
  }
}
import java.util.ArrayList

object WarDriver{
  var warCount:Int = 0
  var fightCount:Int = 0
  def war(tie:Int, lastBattle:Array[Int], data:DataHolder):Unit={
    warCount = warCount + 1
    var winners:ArrayList[Int] = new ArrayList[Int](2)
    for(i <- 0 until lastBattle.length){
      if(data.getCardValue(lastBattle(i)) == tie){
        winners.add(i)
      }
    }
    {
      var i:Int = 0
      while(i < winners.size()){
        if(data.getCardsLeft(winners.get(i)) < 1){
          data.killDead
          i = i - 1
        }
        i = i + 1
      }
    }
    if(winners.size == 1){
      data.discardTo(winners.get(0), lastBattle)
    }
    if(winners.size <= 0){
      println("ERROR, NO LIVING AFTER WAR (Lies...)")
      throw new Error("ERROR, NO LIVING AFTER WAR (Lies...)")
    }
    var stack:ArrayList[Array[Int]] = new ArrayList[Array[Int]](3)
    stack.add(lastBattle)
    stack.add(data.drawEach())
    stack.add(data.drawEach())
    stack.add(data.drawEach())
    var battle:Array[Int] = null
    while(battle == null){
      battle = stack.remove(stack.size() - 1)
    }
    var win = data.getCardValue(battle(0))
    var winCount = 1
    for(i <- 1 until battle.length){
      if(data.getCardValue(battle(i)) == win){
        winCount = winCount + 1
      }
      if(data.getCardValue(battle(i)) > win){
        win = data.getCardValue(battle(i))
        winCount = 1
      }
    }
    if(winCount == 1){
      var winner = -1
      var i = 0
      while(winner == -1){
        if(data.getCardValue(battle(i)) == win){
          winner = i
        }
        i = i + 1
      }
      data.discardTo(winner, battle)
    }else{
      war(win, battle, data)
    }
  }
  def fight(data:DataHolder)={
    fightCount = fightCount + 1
    var battle:Array[Int] = data.drawEach()
    var win = data.getCardValue(battle(0))
    var winCount = 1
    for(i <- 1 until battle.length){
      if(battle(i) == win){
        winCount = winCount + 1
      }
      if(data.getCardValue(battle(i)) > win){
        win = data.getCardValue(battle(i))
        winCount = 1
      }
    }
    if(winCount == 1){
      var winner = -1
      var i = 0
      while(winner == -1){
        if(data.getCardValue(battle(i)) == win){
          winner = i
        }
        i = i + 1
      }
      data.discardTo(winner, battle)
    }else{
      war(win, battle, data)
    }
    data.killDead()
  }
  def play(playerNames:Array[String], deck:Array[Int], deckValuer:(Int)=>Int)={
    if(playerNames.length <= 1){
      println("Not Enough Players 2+ Needed!")
      throw new Error("Not Enough Players 2+ Needed!")
    }
    println("Players:   "+playerNames.length)
    var data:DataHolder = new DataHolder(playerNames, deck, deckValuer)
    println("Deck Size: " + deck.length)
    data.build()
    println("War!")
    println("")
    while(!data.hasWon()){
      fight(data)
    }
    println("Players Won:")
    printList(data.myPlayerNames)
    println("War is Over.")
  }
  def shuffle(pile:ArrayList[Int]):ArrayList[Int]={
    val aFourth:Int = pile.size / 4
    val aFifth:Int = pile.size / 5
    val aTenth:Int = pile.size / 10
    var piles:Array[ArrayList[Int]] = Array((new ArrayList[Int](aFourth)), (new ArrayList[Int](aFourth)))
    var splitAt:Int = pile.size / 2
    if(((scala.math.random * pile.size()).toInt / 2) < aTenth){
      splitAt = splitAt + (scala.math.random * aFifth).toInt - 5
    }
    while(pile.size > 0){
      if(splitAt > 0){
        piles(0).add(pile.remove(0))
        splitAt = splitAt - 1
      }else{
        piles(1).add(pile.remove(0))
      }
    }
    var myPile:ArrayList[Int] = new ArrayList[Int]((piles(0).size + piles(1).size))
    var amount = 1
    var pileWorking = 0
    while((piles(0).size + piles(1).size) > 0){
      if((scala.math.random * 50) < 10){
        amount = (scala.math.random * 6).toInt
      }
      while(amount > 0){
        if(piles(pileWorking).size > 0){
          myPile.add(piles(pileWorking).remove(0))
        }
        amount = amount - 1
      }
      amount = 1
      pileWorking = (pileWorking + 1) % 2
    }
    return myPile
  }
  def printList(list:ArrayList[_])={
    for(i <- 0 until list.size){
      println(i + ": " + list.get(i))
    }
  }
  def main(args:Array[String])={
    var deck = new Array[Int](52)
    for(i <- 0 until deck.length){
      deck(i) = i
    }
    var players:Array[String] = args
    if(players.length <= 1){
      players = Array("P1", "P2")
    }
    play(players, deck, deckValuer52Standard)
    println("Fights:  " + fightCount)
    fightCount = 0
    println("Battles: " + warCount)
    warCount = 0
  }
  def deckValuer52Standard(cardID:Int):Int={
    return cardID % 13
  }
  class DataHolder(playerNames:Array[String], rawDeck:Array[Int], cardValuer:(Int) => Int){
    var deck:ArrayList[ArrayList[Int]] = null
    var discard:ArrayList[ArrayList[Int]] = null
    var myPlayerNames:ArrayList[String] = null
    def killDead()={
      var playerID = 0
      while(playerID < deck.size()){
        if(getCardsLeft(playerID) > 1){
          playerID = playerID + 1
        }else{
          deck.remove(playerID)
          discard.remove(playerID)
          println("Player '" + myPlayerNames.remove(playerID) + "' has Lost.")
        }
      }
    }
    def hasWon():Boolean={
      var players = 0
      for(i <- 0 until deck.size()){
        if(getCardsLeft(i) > 0){
          players = players + 1
        }
      }
      return players <= 1
    }
    def build()={
      deck = new ArrayList[ArrayList[Int]](playerNames.length)
      discard = new ArrayList[ArrayList[Int]](playerNames.length)
      myPlayerNames = new ArrayList[String](playerNames.length)
      for(i <- 0 until playerNames.length){
        myPlayerNames.add(playerNames(i))
        deck.add(new ArrayList[Int](rawDeck.length / playerNames.length))
        discard.add(new ArrayList[Int](rawDeck.length / playerNames.length))
      }
      var rootDeck:ArrayList[Int] = new ArrayList[Int](rawDeck.size)
      for(i <- 0 until rawDeck.length){
        rootDeck.add(rawDeck(i))
      }
      for(i <- 0 to 60){
        rootDeck = shuffle(rootDeck)
      }
      var player = 0
      while(rootDeck.size > 0){
        deck.get(player).add(rootDeck.remove(0))
        player = (player + 1) % deck.size()
      }
    }
    def discardTo(player:Int, pile:Array[Int])={
      for(card <- 0 until pile.length){
        discard.get(player).add(pile(card))
      }
    }
    def getCardValue(card:Int):Int={
      return cardValuer(card)
    }
    def getCardsLeft(player:Int):Int={
      return deck.get(player).size() + discard.get(player).size()
    }
    def draw(player:Int):Int={
      if(deck.get(player).size <= 0){
        if(discard.get(player).size() <= 0){
          throw new Error("No Deck")
        }else{
          deck.set(player,shuffle(shuffle(discard.get(player))))
          discard.set(player, new ArrayList[Int](0))
        }
      }
      return deck.get(player).remove(0)
    }
    def drawEach():Array[Int]={
      for(i <- 0 until deck.size()){
        if(getCardsLeft(i) <= 0){
          return null
        }
      }
      var drawing:Array[Int] = new Array[Int](deck.size())
      for(i <- 0 until deck.size()){
        drawing(i) = draw(i)
      }
      return drawing
    }
  }
}
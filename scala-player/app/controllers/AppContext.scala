package controllers

import javax.inject.Inject

import com.google.inject.Singleton
import com.kli.hot.potato.v0.models.Player
import play.api.Configuration

import scala.collection.mutable.ListBuffer


@Singleton
class AppContext @Inject() (config: Configuration) {
  val startPort = config.getInt("start_port").get
  val endPort = config.getInt("end_port").get
  val ownPort = config.getInt("http.port").get

  val self = Player(s"player-$ownPort", ownPort)
  var opponents = new ListBuffer[Player]()

  (startPort to endPort).filterNot(_ == ownPort).foreach{ port =>
    opponents += Player(s"player-$port", port)
  }

  def getRandomOpponent: Player = {
    val r = scala.util.Random
    val i = r.nextInt(opponents.length)
    opponents(i)
  }

  def removeOpponent(player: Player): Unit = {
    opponents -= player
  }

  def numOpponents = opponents.length
}

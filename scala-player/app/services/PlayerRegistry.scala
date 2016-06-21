package services

import java.net.InetSocketAddress
import javax.inject.{Inject, Singleton}

import com.kli.hot.potato.v0.models.Player
import com.kli.hot.potato.v0.models.json._
import com.loopfor.zookeeper.{ACL, Configuration => ZkConfig, Ephemeral, Zookeeper}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.{Configuration, Logger}

import scala.concurrent.Future
import scala.util.{Random, Try}



@Singleton
class PlayerRegistry @Inject() (config: Configuration, appLifeCycle: ApplicationLifecycle){
  val address = new InetSocketAddress("localhost", 2181)
  val zkConfig = ZkConfig(address::Nil)
  val zkClient = Zookeeper(zkConfig)

  val ownPort = config.getInt("http.port").get
  val self = Player(s"player-$ownPort", ownPort)

  // Register ones-self with zookeeper
  val selfPath = s"/hot-potato/${self.name}.json"
  Logger.info(s"Registering player @ $selfPath")
  zkClient.sync.create(selfPath, Json.toJson(self).toString.getBytes, ACL.AnyoneAll, Ephemeral)

  appLifeCycle.addStopHook { () =>
    Try {
      unregisterSelf()
    }

    Future.successful[Unit]()
  }


  def getRandomOpponent: Option[Player] = {
    val opponents = zkClient.sync.children("/hot-potato").filterNot( nodeName => nodeName == s"${self.name}.json" )
    if (opponents.isEmpty) {
      None
    } else {
      val i = Random.nextInt(opponents.length)
      val (data, status) = zkClient.sync.get(s"/hot-potato/${opponents(i)}")
      val player = Json.fromJson[Player](Json.parse(data)).get

      Some(player)
    }
  }


  def getPlayers: Seq[Player] = {
    val nodes = zkClient.sync.children("/hot-potato")
    nodes.map{ node =>
      val (data, status) = zkClient.sync.get(s"/hot-potato/$node")
      Json.fromJson[Player](Json.parse(data)).get
    }
  }

  def unregisterSelf(): Unit = zkClient.sync.delete(selfPath, None)



}

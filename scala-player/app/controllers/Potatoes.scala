package controllers


import javax.inject._
import akka.actor.ActorSystem
import com.kli.hot.potato.v0.Client
import com.kli.hot.potato.v0.errors.ValueResponse
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import services.PlayerRegistry
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.kli.hot.potato.v0.models._
import com.kli.hot.potato.v0.models.json._
import scala.util.Random


@Singleton
class Potatoes @Inject()(actorSystem: ActorSystem, players: PlayerRegistry)(implicit exec: ExecutionContext) extends Controller {

  val THROW_DURATION = 4.seconds


  def scheduleShutdown(): Unit = {
    players.unregisterSelf
    actorSystem.scheduler.scheduleOnce(1.second) { System.exit(0) }
  }

  def scheduleNewThrow(potato: Potato): Unit = actorSystem.scheduler.scheduleOnce(THROW_DURATION){

    players.getRandomOpponent.map{ opponent =>
      val newPotato = potato.copy(toss = potato.toss + 1, player = players.self)

      Logger.info(s"""Tossing potato (${newPotato.toss} of ${newPotato.maxToss}) to ${opponent.name}""")

      val url = s"http://localhost:${opponent.port}"
      val client = new Client(url)
      val response = client.Potatoes.post(newPotato)

      response.onFailure {
        case r: ValueResponse if r.response.status == GONE =>
          scheduleNewThrow(potato.copy(toss = 0, maxToss = Random.nextInt(10) + 1, player = players.self))
        case e => throw e
      }

      Unit
    }.getOrElse {
      Logger.info("Wins!!!")
      scheduleShutdown()
    }
  }

  def post() = Action(parse.json[Potato]) { request =>
    val potato: Potato  = request.body
    Logger.info(s"Caught potato from ${potato.player.name}")

    if (potato.toss >= potato.maxToss) {
      Logger.info(s"BOOOOOOMMMMMMMM")
      scheduleShutdown()
      Gone("I'm Dead :(")
    } else {
      Logger.info(s"Winding up for throw...($THROW_DURATION)")
      scheduleNewThrow(potato)
      Ok(Json.toJson(potato))
    }
  }

}

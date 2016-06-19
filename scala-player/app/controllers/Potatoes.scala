package controllers


import javax.inject._
import akka.actor.ActorSystem
import com.kli.hot.potato.v0.Client
import com.kli.hot.potato.v0.errors.ValueResponse
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import com.kli.hot.potato.v0.models._
import com.kli.hot.potato.v0.models.json._


@Singleton
class Potatoes @Inject()(actorSystem: ActorSystem, context: AppContext)(implicit exec: ExecutionContext) extends Controller {

  def scheduleShutdown(): Unit = actorSystem.scheduler.scheduleOnce(1.second) { System.exit(0) }

  def scheduleNewThrow(potato: Potato): Unit = {

    actorSystem.scheduler.scheduleOnce(5.seconds){
      val nextPlayer = context.getRandomOpponent
      val nextPotato = potato.copy(toss = potato.toss + 1, player = context.self)

      Logger.info(s"""Tossing potato to ${nextPlayer.name}""")

      val url = s"http://localhost:${nextPlayer.port}"
      val client = new Client(url)
      val response = client.Potatoes.post(nextPotato)

      response.onFailure{
        case e: ValueResponse =>
          context.removeOpponent(nextPlayer)
          if(context.numOpponents > 0)
            scheduleNewThrow(potato)
          else {
            Logger.info(s"""${context.self.name} wins!!!""")
            scheduleShutdown()
          }

        case e => throw e
      }

    }
  }

  def post() = Action(parse.json[Potato]) { request =>
    val potato: Potato  = request.body
    Logger.info(s"Received the potato from ${potato.player.name}")

    if (potato.toss == potato.maxToss) {
      Logger.info("BOOOOOOMMMMMMMM")
      scheduleShutdown()
      Gone("I'm Dead :(")
    } else {
      Logger.info("Winding up for throw...")
      scheduleNewThrow(potato)
      Ok(Json.toJson(potato))
    }
  }

}

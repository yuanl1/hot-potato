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
import scala.util.Random


@Singleton
class Potatoes @Inject()(actorSystem: ActorSystem, context: AppContext)(implicit exec: ExecutionContext) extends Controller {

  def scheduleShutdown(): Unit = actorSystem.scheduler.scheduleOnce(1.second) { System.exit(0) }

  def scheduleNewThrow(potato: Potato): Unit = {

    actorSystem.scheduler.scheduleOnce(5.seconds){
      val opponent = context.getRandomOpponent

      Logger.info(s"""${context.self.name} - Tossing potato to ${opponent.name}""")

      val url = s"http://localhost:${opponent.port}"
      val client = new Client(url)
      val response = client.Potatoes.post(potato.copy(toss = potato.toss + 1, player = context.self))

      response.onFailure{
        case e: ValueResponse =>
          context.removeOpponent(opponent)
          if(context.numOpponents > 0)
            // Construct a new potato here with a random max_toss
            scheduleNewThrow(potato.copy(toss = 0, maxToss = Random.nextInt(10), player = context.self))
          else {
            Logger.info(s"""${context.self.name} - Wins!!!""")
            scheduleShutdown()
          }

        case e => throw e
      }

    }
  }

  def post() = Action(parse.json[Potato]) { request =>
    val potato: Potato  = request.body
    Logger.info(s"${context.self.name} - Received the potato from ${potato.player.name}")

    if (potato.toss == potato.maxToss) {
      Logger.info(s"${context.self.name} - BOOOOOOMMMMMMMM")
      scheduleShutdown()
      Gone("I'm Dead :(")
    } else {
      Logger.info(s"${context.self.name} - Winding up for throw...")
      scheduleNewThrow(potato)
      Ok(Json.toJson(potato))
    }
  }

}

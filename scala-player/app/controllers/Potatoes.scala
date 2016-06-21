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


  def post() = Action {

    //TODO: if toss >= maxToss respond with "Gone" and schedule shutdown
    //TODO: if toss < maxToss respond with "Ok" and schedule new throw
    NotImplemented
  }

}

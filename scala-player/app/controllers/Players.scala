package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import services.PlayerRegistry
import scala.concurrent.ExecutionContext
import com.kli.hot.potato.v0.models._
import com.kli.hot.potato.v0.models.json._

/**
  * Created by kli on 6/18/16.
  */
@Singleton
class Players @Inject() (players: PlayerRegistry)(implicit exec: ExecutionContext) extends Controller {

  def get = Action {
    Ok(Json.toJson(players.getPlayers))
  }

}

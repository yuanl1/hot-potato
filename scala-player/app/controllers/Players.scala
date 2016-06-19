package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.ExecutionContext
import com.kli.hot.potato.v0.models._
import com.kli.hot.potato.v0.models.json._

/**
  * Created by kli on 6/18/16.
  */
@Singleton
class Players @Inject() (context: AppContext)(implicit exec: ExecutionContext) extends Controller {

  def getByKind(kind: PlayerKind) = Action {
    kind match {
      case PlayerKind.Self => Ok(Json.toJson(List(context.self)))
      case PlayerKind.Opponent => Ok(Json.toJson(context.opponents))
    }
  }

}

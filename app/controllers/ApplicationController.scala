package controllers

import javax.inject._
import akka.actor.ActorSystem
import domain.{User, Users}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import services.UserDataServiceImpl
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Configuration

@Singleton
class ApplicationController @Inject()(cc: ControllerComponents, us: UserDataServiceImpl, config: Configuration) extends AbstractController(cc) {

  implicit val userFormat: OWrites[User] = Json.writes[User]
  implicit val usersFormat: OWrites[Users] = Json.writes[Users]

  trait MyExecutionContext extends ExecutionContext

  class MyExecutionContextImpl @Inject()(myExecutionContext: MyExecutionContext, system: ActorSystem)
    extends CustomExecutionContext(system, "listenup.executor") with MyExecutionContext

  def retrieveAllUsers(): Action[AnyContent] = Action.async {
    us.retrieveAll().map(res => Ok(Json.toJson(Users(res.toArray, config.get[String]("users.uri")))))
  }

  def retrieveSingleUser(username: String): Action[AnyContent] = Action.async {
      us.retrieveSingle(username).map(res => Ok(Json.toJson(res)))
    }
}

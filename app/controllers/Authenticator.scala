package controllers

import models.User
import play.api.libs.json.Json
import play.api.mvc._
import repository.TaskRepository
import securesocial.core.{Events, LogoutEvent, SecureSocial}
import security.SecuredComponent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Authenticator(taskRepository: TaskRepository) extends Controller with SecureSocial[User] with SecuredComponent {

  // Workaround to waiting for ReactiveMongoDB to authenticate with MongoLabs
  def getLogin = SecuredAction.async { implicit request => implicit val user = request.user
    taskRepository.count.map { count =>
      Ok(Json.toJson(request.user))
    }
  }

  def logoff = UserAwareAction.async { implicit request =>
    val result = for {
      user <- request.user
      authenticator <- request.authenticator
    } yield {
      authenticator.discarding(NoContent.withSession(Events.fire(new LogoutEvent(user)).getOrElse(request.session)))
    }
    result.getOrElse(Future.successful(Unauthorized))
  }
}

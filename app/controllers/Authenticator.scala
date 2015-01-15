package controllers

import models.User
import play.api.libs.json.Json
import play.api.mvc._
import securesocial.core.{Events, LogoutEvent, SecureSocial}
import security.SecuredComponent

import scala.concurrent.Future

class Authenticator extends Controller with SecureSocial[User] with SecuredComponent {

  def getLogin = SecuredAction { request =>
    Ok(Json.toJson[User](request.user))
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

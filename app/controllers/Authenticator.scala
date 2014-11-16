package controllers

import models.User
import play.api.libs.json.Json
import play.api.mvc._
import securesocial.core.{LogoutEvent, Events, RuntimeEnvironment, SecureSocial}

import scala.concurrent.Future

class Authenticator(override implicit val env: RuntimeEnvironment[User]) extends Controller with SecureSocial[User] {

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

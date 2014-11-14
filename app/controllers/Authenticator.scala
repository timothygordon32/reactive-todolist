package controllers

import models.User
import play.api.libs.json.Json
import play.api.mvc._
import securesocial.core.{RuntimeEnvironment, SecureSocial}

class Authenticator(override implicit val env: RuntimeEnvironment[User]) extends Controller with SecureSocial[User] {

  def getLogin = SecuredAction { request =>
    Ok(Json.toJson[User](request.user))
  }

  def logoff = SecuredAction { request =>
    NoContent.discardingCookies(DiscardingCookie("id"))
  }
}

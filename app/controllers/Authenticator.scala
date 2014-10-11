package controllers

import models.User
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc._
import securesocial.core.{SecureSocial, RuntimeEnvironment}
import security.{Authenticated, PasswordHash}

class Authenticator(override implicit val env: RuntimeEnvironment[User]) extends Controller with SecureSocial[User] {

  case class LoginWithPassword(username: String, password: String) {
    def sanitise = Login(username)
  }

  implicit val reads = Json.reads[LoginWithPassword]

  case class Login(username: String)

  implicit val writes = Json.writes[Login]

  def login = Action(parse.json) { implicit request =>
    request.body.validate[LoginWithPassword].fold(
      _ => BadRequest,
      login => {
        if (validate(login.username, login.password))
          Ok(Json.toJson(login.sanitise)).withNewSession.withSession(Security.username -> login.username)
        else Unauthorized
      })
  }

  private def validate(username: String, password: String) = {
    Play.current.configuration.getString(s"users.$username") match {
      case Some(hash) => PasswordHash.verify(password, hash)
      case None => !Play.isProd && username == "testuser" && password == "secret"
  }
  }

  def getLogin = Authenticated { request =>
    Ok(Json.obj(Security.username -> request.session.get(Security.username)))
  }

  def logoff = Authenticated { request =>
    NoContent.withNewSession
  }
}

package controllers

import play.api.Play
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

object Authenticator extends Controller {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    )
  )

  def login = Action { implicit request =>
    val (username, password) = loginForm.bindFromRequest.get
    if (validate(username, password)) Ok.withNewSession.withSession(Security.username -> username)
    else Unauthorized
  }

  private def validate(username: String, password: String) = {
    Play.current.configuration.getString(s"users.$username") match {
      case Some(hash) => PasswordHash.verify(password, hash)
      case None => !Play.isProd && username == "testuser" && password == "secret"
    }
  }

  def getUsername = Authenticated { request =>
    Ok(Json.obj(Security.username -> request.session.get(Security.username)))
  }
}

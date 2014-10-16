package controllers

import models.User
import play.api.mvc._
import securesocial.core.{RuntimeEnvironment, SecureSocial}

class Home(override implicit val env: RuntimeEnvironment[User]) extends Controller with SecureSocial[User] {

  def index = Action {
    Ok(views.html.tasks())
  }
}

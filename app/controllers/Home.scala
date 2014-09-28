package controllers

import play.api.mvc._

object Home extends Controller {

  def index = Action {
    Ok(views.html.tasks())
  }
}

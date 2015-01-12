package controllers

import play.api.mvc._

class Home extends Controller {

  def index = Action {
    Ok(views.html.tasks())
  }
}

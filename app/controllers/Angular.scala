package controllers

import play.api.mvc.{Action, Controller}

object Angular extends Controller {

    def tasks = Action { implicit request =>
      Ok(views.html.tasks())
    }
}

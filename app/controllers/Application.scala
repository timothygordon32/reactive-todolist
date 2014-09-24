package controllers

import models.Task
import models.Task._
import play.api.Play
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

object Application extends Controller {

  def index = Action {
    Ok(views.html.tasks())
  }
}

package controllers

import models.Task
import models.Task._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import repository.TaskRepository

import scala.concurrent.Future

object Tasks extends Controller {

  def create = Action.async(parse.json) { implicit request =>
    request.body.validate[Task].fold(
      valid = {
        task => TaskRepository.create(task).map(result => Ok(Json.toJson(result)))
      },
      invalid = {
        errors => Future.successful(BadRequest(JsError.toFlatJson(errors)))
      }
    )
  }

  def list = Action.async {
    TaskRepository.findAll.map{ tasks => Ok(Json.toJson(tasks)) }
  }

  def get(id: String) = Action.async {
    TaskRepository.find(id).map {
      case Some(task) => Ok(Json.toJson(task))
      case None => NotFound
    }
  }

  def delete(id: String) = Action.async {
    TaskRepository.deleteTask(id).map {
      deleted => if (deleted) Ok else NoContent
    }
  }
}

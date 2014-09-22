package controllers

import models.Task._
import models.{Task, User}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import repository.UserTaskRepository

import scala.concurrent.Future

object UserTasks extends Controller {

  implicit def toUser(implicit request: Security.AuthenticatedRequest[_, User]): User = request.user

  def create = Authenticated.async(parse.json) { implicit request =>

    request.body.validate[Task].fold(
      valid = {
        task => UserTaskRepository.create(task).map(result => Ok(Json.toJson(result)))
      },
      invalid = {
        errors => Future.successful(BadRequest(JsError.toFlatJson(errors)))
      }
    )
  }

  def list = Authenticated.async { implicit request =>
    UserTaskRepository.findAll.map{ tasks => Ok(Json.toJson(tasks)) }
  }

  def delete(id: String) = Authenticated.async { implicit request =>
    UserTaskRepository.deleteTask(id).map {
      deleted => if (deleted) Ok else NotFound
    }
  }
}

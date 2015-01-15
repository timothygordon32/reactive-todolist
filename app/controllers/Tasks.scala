package controllers

import models.Task._
import models.{Task, User}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import repository.TaskRepository
import securesocial.core.SecureSocial
import security.SecuredComponent

import scala.concurrent.Future
import scala.util.{Failure, Success}

class Tasks(taskRepository: TaskRepository) extends Controller with SecureSocial[User] with SecuredComponent {

  implicit def toUser(implicit request: SecuredRequest[_]): User = request.user

  def create = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Task].fold(
      valid = {
        task => taskRepository.create(task).map(result => Ok(Json.toJson(result)))
      },
      invalid = {
        errors => Future.successful(BadRequest(JsError.toFlatJson(errors)))
      }
    )
  }

  def list = SecuredAction.async { implicit request =>
    taskRepository.findAll.map{ tasks => Ok(Json.toJson(tasks)) }
  }

  def get(id: String) = SecuredAction.async { implicit request =>
    taskRepository.find(id).map {
      case Some(task) => Ok(Json.toJson(task))
      case None => NotFound
    }
  }

  def update(id: String) = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Task].fold(
      valid = {
        task => BSONObjectID.parse(id) match {
          case Success(bsonId) => taskRepository.update(task.copy(id = Some(bsonId))).map {
            case true => NoContent
            case false => NotFound
          }
          case Failure(e) => Future.successful(BadRequest)
        }
      },
      invalid = {
        errors => Future.successful(BadRequest(JsError.toFlatJson(errors)))
      }
    )
  }

  def delete(id: String) = SecuredAction.async { implicit request =>
    taskRepository.delete(id).map {
      deleted => if (deleted) NoContent else NotFound
    }
  }

  def deleteDone() = SecuredAction.async { implicit request =>
    taskRepository.deleteDone.map {
      deleted => Redirect(controllers.routes.Tasks.list())
    }
  }
}


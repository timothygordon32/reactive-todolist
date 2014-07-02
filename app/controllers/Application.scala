package controllers

import models.Task
import models.Task._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import repository.TaskRepository

import scala.concurrent.Future

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.tasks)
  }

  def tasks = Action.async { implicit request =>
    TaskRepository.findAll.map {
      tasks => Ok(views.html.index(tasks, taskForm))
    }
  }

  def newTask = Action.async { implicit request =>
    taskForm.bindFromRequest.fold (
      errors => {
        TaskRepository.findAll.map {
          tasks => BadRequest(views.html.index(tasks, errors))
        }
      },
      label => {
        TaskRepository.create(new Task(None, label)).map(_ => Redirect(routes.Application.tasks))
      }
    )
  }

  def echo = Action.async(parse.json) { implicit request =>
    Future.successful(
      request.body.validate[Task].fold(
        valid = {
          task => Ok(Json.toJson[Task](task))
        },
        invalid = {
          errors => BadRequest(JsError.toFlatJson(errors))
        }
      )
    )
  }

  def createTask = Action.async(parse.json) { implicit request =>
    request.body.validate[Task].fold(
      valid = {
        task => TaskRepository.create(task).map(result => Ok(Json.toJson(result)))
      },
      invalid = {
        errors => Future.successful(BadRequest(JsError.toFlatJson(errors)))
      }
    )
  }

  def getTask(id: String) = Action.async {
    TaskRepository.find(id).map {
      case Some(task) => Ok(Json.toJson(task))
      case None => NotFound
    }
  }

  def deleteTask(id: String) = Action.async {
    TaskRepository.deleteTask(id).map(_ => Redirect(routes.Application.tasks))
  }

  def deleteTaskJson(id: String) = Action.async {
    TaskRepository.deleteTask(id).map {
      deleted => if (deleted) Ok else NoContent
    }
  }

  import play.api.data.Forms._
  import play.api.data._

  val taskForm = Form(
    "label" -> nonEmptyText
  )
}

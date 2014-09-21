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
import repository.TaskRepository

import scala.concurrent.Future

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.tasks)
  }

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    )
  )

  def validate(username: String, password: String) = {
    Play.current.configuration.getString(s"users.$username") match {
      case Some(hash) => PasswordHash.verify(password, hash)
      case None => !Play.isProd && username == "testuser" && password == "secret"
    }
  }

  def login = Action { implicit request =>
    val (username, password) = loginForm.bindFromRequest.get
    if (validate(username, password)) Ok.withNewSession.withSession(Security.username -> username) else Unauthorized
  }

  def getUsername = Action { request =>
    Ok(Json.obj(Security.username -> request.session.get(Security.username)))
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

  def list = Action.async {
    TaskRepository.findAll.map{ tasks => Ok(Json.toJson(tasks)) }
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

package controllers

import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future

import models.Task
import models.Task._

object Application extends Controller with MongoController {

  implicit val reader = Task.TaskBSONReader

  lazy val collection = db[BSONCollection]("tasks")

  def index = Action {
    Redirect(routes.Application.tasks)
  }

  def tasks = Action.async { implicit request =>
    findAllTasks.map {
      tasks => Ok(views.html.index(tasks, taskForm))
    }
  }

  def newTask = Action.async { implicit request =>
    taskForm.bindFromRequest.fold (
      errors => {
        findAllTasks.map {
          tasks => BadRequest(views.html.index(tasks, errors))
        }
      },
      label => {
        collection.insert(new Task(Some(BSONObjectID.generate), label)).map(_ => Redirect(routes.Application.tasks))
      }
    )
  }

  def findAllTasks = {
    val query = BSONDocument()
    collection.find(query).cursor[Task].collect[List]()
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
        task => collection.insert(task).map(_ => Ok(Json.toJson(task)))
      },
      invalid = {
        errors => Future.successful(BadRequest(JsError.toFlatJson(errors)))
      }
    )
  }

  def getTask(id: String) = Action.async {
    val query = BSONDocument("_id" -> new BSONObjectID(id))
    collection.find(query).cursor[Task].headOption.map {
      case Some(task) => Ok(Json.toJson(task))
      case None => NotFound
    }
  }

  def deleteTask(id: String) = Action.async {
    collection.remove(BSONDocument("_id" -> new BSONObjectID(id))).map(_ => Redirect(routes.Application.tasks))
  }

  import play.api.data._
  import play.api.data.Forms._

  val taskForm = Form(
    "label" -> nonEmptyText
  )
}

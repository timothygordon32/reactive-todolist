package repository

import models.{User, Task}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaskRepository {
  import play.api.Play.current
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val taskWrites: Writes[Task] = (
    (__ \ "_id").write[Option[BSONObjectID]] and
      (__ \ "label").write[String]
    )(unlift(Task.unapply))

  implicit val taskReads: Reads[Task] = (
    (__ \ "_id").read[Option[BSONObjectID]] and
      (__ \ "label").read[String]
    )(Task.apply _)

  def db = ReactiveMongoPlugin.db

  lazy val collection = db[JSONCollection]("userTasks")

  def create(task: Task)(implicit user: User): Future[Task] = {
    val toCreate = task match {
      case Task(None, _) => task.copy(id = Some(BSONObjectID.generate))
      case Task(Some(_), _) => task
    }

    collection.insert(Json.toJson(toCreate).as[JsObject] ++ Json.obj("user" -> user.username)).map {
      lastError =>
        Logger.info(s"Created task $toCreate")
        toCreate
    }
  }

  def findAll(implicit user: User): Future[List[Task]] = {
    collection.find(Json.obj("user" -> user.username)).cursor[Task].collect[List]()
  }

  def find(id: String)(implicit user: User): Future[Option[Task]] = {
    val byId = Json.obj("_id" -> BSONObjectID(id), "user" -> user.username)
    collection.find(byId).cursor[Task].headOption
  }

  def deleteTask(id: String)(implicit user: User): Future[Boolean] = {
    collection.remove(Json.obj("_id" -> BSONObjectID(id), "user" -> user.username)).map {
      lastError => lastError.updated == 1
    }
  }
}

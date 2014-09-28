package repository

import models.{User, Task}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaskRepositoryFormats {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val taskWrites: Writes[Task] = (
    (__ \ "_id").write[Option[BSONObjectID]] and
      (__ \ "text").write[String] and
      (__ \ "done").write[Boolean]
    )(unlift(Task.unapply))

  implicit val taskReads: Reads[Task] = (
    (__ \ "_id").read[Option[BSONObjectID]] and
      ((__ \ "label").read[String] or (__ \ "text").read[String]) and
      (__ \ "done").read[Boolean]
    )(Task.apply _)
}

object TaskRepository {
  import play.api.Play.current
  import repository.TaskRepositoryFormats._

  private def db = ReactiveMongoPlugin.db

  private lazy val collection = db[JSONCollection]("userTasks")

  private val indexesCreated = collection.indexesManager.ensure(Index(Seq("user" -> IndexType.Ascending)))

  indexesCreated.map {
    case true => Logger.info("Created index for 'user'")
    case false => Logger.info("Index for 'user' already exists")
  }

  def indexes() = indexesCreated.flatMap(_ => collection.indexesManager.list())

  def create(task: Task)(implicit user: User): Future[Task] = {
    val toCreate = task match {
      case Task(None, _, _) => task.copy(id = Some(BSONObjectID.generate))
      case Task(Some(_), _, _) => task
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

  def update(task: Task)(implicit user: User): Future[Boolean] = {
    collection.save(Json.toJson(task).as[JsObject] ++ Json.obj("user" -> user.username)).map {
      lastError => lastError.updated == 1
    }
  }

  def delete(id: String)(implicit user: User): Future[Boolean] = {
    collection.remove(Json.obj("_id" -> BSONObjectID(id), "user" -> user.username)).map {
      lastError => lastError.updated == 1
    }
  }
}

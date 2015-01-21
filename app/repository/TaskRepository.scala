package repository

import models.{Task, User}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.commands.Count
import repository.index.{IndexedCollection, DelayedIndexOperations, Ensure}

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

trait TaskRepository {

  def indexes(): Future[Seq[Index]]

  def create(task: Task)(implicit user: User): Future[Task]

  def count(implicit user: User): Future[Int]

  def findAll(implicit user: User): Future[Seq[Task]]

  def find(id: String)(implicit user: User): Future[Option[Task]]

  def update(task: Task)(implicit user: User): Future[Boolean]

  def delete(id: String)(implicit user: User): Future[Boolean]

  def deleteDone(implicit user: User): Future[Int]
}

object MongoTaskRepository extends TaskRepository with DelayedIndexOperations {

  import play.api.Play.current
  import repository.TaskRepositoryFormats._

  def db = ReactiveMongoPlugin.db

  private val indexedCollection = new IndexedCollection(
    db[JSONCollection]("userTasks"),
    Seq(Ensure(Index(Seq("user" -> IndexType.Ascending), Some("user"), background = true))))

  private val collection = indexedCollection.collection

  def indexes() = indexedCollection.indexes()

  def create(task: Task)(implicit user: User): Future[Task] = {
    val toCreate = task match {
      case Task(None, _, _) => task.copy(id = Some(BSONObjectID.generate))
      case Task(Some(_), _, _) => task
    }

    collection.insert(Json.toJson(toCreate).as[JsObject] ++ Json.obj("user" -> user.id)).map(_ => toCreate)
  }

  def count(implicit user: User): Future[Int] =
    collection.db.command(
      Count(
        collection.name,
        Some(Json.obj("user" -> user.id).as[BSONDocument])
      ))

  def findAll(implicit user: User): Future[Seq[Task]] =
    collection.find(Json.obj("user" -> user.id)).sort(Json.obj("_id" -> 1)).cursor[Task].collect[Seq]()

  def find(id: String)(implicit user: User): Future[Option[Task]] = {
    val byId = Json.obj("_id" -> BSONObjectID(id), "user" -> user.id)
    collection.find(byId).cursor[Task].headOption
  }

  def update(task: Task)(implicit user: User): Future[Boolean] = {
    collection.save(Json.toJson(task).as[JsObject] ++ Json.obj("user" -> user.id)).map {
      lastError => lastError.updated == 1
    }
  }

  def delete(id: String)(implicit user: User): Future[Boolean] = {
    collection.remove(Json.obj("_id" -> BSONObjectID(id), "user" -> user.id)).map {
      lastError => lastError.updated == 1
    }
  }

  def deleteDone(implicit user: User): Future[Int] = {
    collection.remove(Json.obj("done" -> true, "user" -> user.id)).map {
      lastError => lastError.updated
    }
  }
}

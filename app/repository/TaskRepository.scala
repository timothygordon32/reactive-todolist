package repository

import models.{User, Task}
import play.api.libs.functional.syntax._
import play.api.libs.iteratee.{Iteratee, Enumerator}
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

object TaskRepository extends Indexed {
  import play.api.Play.current
  import repository.TaskRepositoryFormats._

  def db = ReactiveMongoPlugin.db

  private lazy val collection = db[JSONCollection]("userTasks")

  private val indexesCreated = ensureIndex(collection, Index(Seq("user" -> IndexType.Ascending), Some("user")))

  def indexes(): Future[List[Index]] = for {
    _ <- indexesCreated
    indexes <- collection.indexesManager.list()
  } yield indexes

  def create(task: Task)(implicit user: User): Future[Task] = {
    create(task, user.username)
  }

  def create(task: Task, userId: String): Future[Task] = {
    val toCreate = task match {
      case Task(None, _, _) => task.copy(id = Some(BSONObjectID.generate))
      case Task(Some(_), _, _) => task
    }

    collection.insert(Json.toJson(toCreate).as[JsObject] ++ Json.obj("user" -> userId)).map(_ => toCreate)
  }

  def findAll(implicit user: User): Future[List[Task]] = findAll(user.username)

  def findAll(userId: String): Future[List[Task]] =
    collection.find(Json.obj("user" -> userId)).sort(Json.obj("_id" -> 1)).cursor[Task].collect[List]()

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

  def deleteDone(implicit user: User): Future[Int] = {
    collection.remove(Json.obj("done" -> true, "user" -> user.username)).map {
      lastError => lastError.updated
    }
  }

  def copy(fromUserId: String, toUserId: String): Future[List[Task]] = {
    def copy(copies: Vector[Task], task: Task): Future[Vector[Task]] = create(task.copy(id = None), toUserId).map(copies :+ _)

    def copyAll(tasks: List[Task]): Future[List[Task]] = {
      val toCopy = Enumerator.enumerate(tasks)
      val copier = Iteratee.foldM(Vector.empty[Task])(copy)

      toCopy.run(copier).map(_.toList)
    }

    for {
      fromTasks <- findAll(fromUserId)
      toTasks <- copyAll(fromTasks)
    } yield toTasks
  }
}

package repository

import models.{Task, User}
import play.api.Play
import play.api.libs.concurrent.Akka
import play.api.libs.functional.syntax._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

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

  def indexes(): Future[Seq[Index]] = for {
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

  def create(task: Task, userId: BSONObjectID): Future[Task] = {
    val toCreate = task match {
      case Task(None, _, _) => task.copy(id = Some(BSONObjectID.generate))
      case Task(Some(_), _, _) => task
    }

    collection.insert(Json.toJson(toCreate).as[JsObject] ++ Json.obj("user" -> userId)).map(_ => toCreate)
  }

  def findAll(implicit user: User): Future[Seq[Task]] = for {
    byUsername <- findAll(user.username)
    byId <- findAll(user.id)
  } yield byUsername ++ byId

  def findAll(userId: String): Future[Seq[Task]] = Future.successful(Seq.empty)
//    collection.find(Json.obj("user" -> userId)).sort(Json.obj("_id" -> 1)).cursor[Task].collect[Seq]()

  def findAll(userId: BSONObjectID): Future[Seq[Task]] =
    collection.find(Json.obj("user" -> userId)).sort(Json.obj("_id" -> 1)).cursor[Task].collect[Seq]()

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

  def copy(fromUserId: String, toUserId: String): Future[Seq[Task]] = {
    def copy(copies: Vector[Task], task: Task): Future[Vector[Task]] = create(task.copy(id = None), toUserId).map(copies :+ _)

    def copyAll(tasks: Seq[Task]): Future[Seq[Task]] = {
      val toCopy = Enumerator.enumerate(tasks)
      val copier = Iteratee.foldM(Vector.empty[Task])(copy)

      toCopy.run(copier)
    }

    for {
      fromTasks <- findAll(fromUserId)
      toTasks <- copyAll(fromTasks)
    } yield toTasks
  }

  def migrateTaskOwnershipToUserObjectId(user: User): Future[Int] = {
    def delayed[T](duration: FiniteDuration) = akka.pattern.after[T](duration, using = Akka.system(Play.current).scheduler) _

    def copy(migrated: Int, task: Task): Future[Int] = delayed(200.milliseconds) {
      collection.save(Json.toJson(task).as[JsObject] ++ Json.obj("user" -> user.id)).map {
        lastError => migrated + lastError.updated
      }
    }

    val toMigrate = collection.find(Json.obj("user" -> user.username)).cursor[Task].enumerate()
    val migrator = Iteratee.foldM(0)(copy)

    toMigrate.run(migrator)
  }
}

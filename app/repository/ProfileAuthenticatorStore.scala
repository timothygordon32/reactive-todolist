package repository

import models.User
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import securesocial.core.authenticator.{Authenticator, AuthenticatorStore}
import time.DateTimeUtils.now

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

trait ProfileAuthenticatorStore[A <: Authenticator[User]] extends AuthenticatorStore[A] with ProfileRepository {

  private val authenticatorIdIndexCreated =
    ensureIndex(collection, Index(Seq("authenticator.id" -> IndexType.Ascending), Some("authenticatorId")))

  override def indexes(): Future[Seq[Index]] = {
    val superIndexes = super.indexes()
    val storeIndexes = for {
      _ <- authenticatorIdIndexCreated
      indexes <- collection.indexesManager.list()
    } yield indexes
    Future.sequence(Seq(superIndexes, storeIndexes)).map(_.flatten)
  }

  case class UserWithAuthenticator(_id: BSONObjectID,
                                   userId: String,
                                   firstName: Option[String],
                                   authenticator: UserAuthenticator) {
    def toUser = User(_id, userId, firstName)
  }

  case class UserAuthenticator(id: String, expirationDate: DateTime, lastUsed: DateTime, creationDate: DateTime)

  def userAuthenticator(authenticator: A): UserAuthenticator =
    UserAuthenticator(
      authenticator.id,
      authenticator.expirationDate,
      authenticator.lastUsed,
      authenticator.creationDate)

  implicit val dateTimeFormat = Formats.dateTimeFormat

  implicit val userAuthenticatorFormat = Json.format[UserAuthenticator]

  implicit val userWithAuthenticatorReads = Json.reads[UserWithAuthenticator]

  def delete(id: String): Future[Unit] = for {
    _ <- collection.update(Json.obj("authenticator.id" -> id), Json.obj("$unset" -> Json.obj("authenticator" -> -1)))
  } yield ()

  def save(authenticator: A, timeoutInSeconds: Int): Future[A] = {
    val userId = authenticator.user.username
    collection.update(
      Json.obj("userId" -> userId),
      Json.obj("$set" -> Json.obj("authenticator" -> userAuthenticator(authenticator)))
    ) map { lastError =>
      if (lastError.updatedExisting) authenticator
      else throw new IllegalStateException(s"Could not find user with userId $userId")
    }
  }

  def find(id: String)(implicit ct: ClassTag[A]): Future[Option[A]] = {
    val selector = Json.obj("authenticator.id" -> id, "authenticator.expirationDate" -> Json.obj("$gte" -> now))
    collection.find(selector).cursor[UserWithAuthenticator].headOption.map(_.map(toAuthenticator))
  }

  def toAuthenticator(userWithAuthenticator: UserWithAuthenticator): A
}

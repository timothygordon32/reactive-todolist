package repository

import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import securesocial.core.providers.MailToken
import time.DateTimeUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TokenRepository extends SecureSocialDatabase {
  private lazy val indexedCollection = new Indexed {
    val collection = db.collection[JSONCollection] ("tokens")

    val emailIndexEnsured =
      ensureIndex(collection, Index(Seq("uuid" -> IndexType.Ascending), Some("uuid")))

    val providerIdUserIdIndexEnsured =
      ensureIndex(collection, Index(Seq("expirationTime" -> IndexType.Ascending), Some("expirationTime")))

    def indexes(): Future[Seq[Index]] = for {
      _ <- emailIndexEnsured
      _ <- providerIdUserIdIndexEnsured
      indexes <- collection.indexesManager.list()
    } yield indexes
  }

  private lazy val collection = indexedCollection.collection

  def indexes() = indexedCollection.indexes()

  private implicit val dateTimeFormat = Formats.dateTimeFormat
  private implicit val tokenFormat = Json.format[MailToken]

  def saveToken(token: MailToken): Future[MailToken] =
    collection.insert(token).map(_ => token)

  def findToken(uuid: String): Future[Option[MailToken]] =
    collection.find(Json.obj("uuid" -> uuid)).cursor[MailToken].headOption

  def deleteToken(uuid: String): Future[Option[MailToken]] = for {
    token <- findToken(uuid)
    _ <- collection.remove(Json.obj("uuid" -> uuid))
  } yield token

  def deleteExpiredTokens(): Unit =
    collection.remove(Json.obj("expirationTime" -> Json.obj("$lt" -> now)))
}

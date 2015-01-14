package repository

import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import repository.index.{DelayedIndexOperations, Ensure, IndexedCollection}
import securesocial.core.providers.MailToken
import time.DateTimeUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TokenRepositoryComponent {
  val tokenRepository: TokenRepository

  trait TokenRepository {
    def indexes(): Future[Seq[Index]]

    def saveToken(token: MailToken): Future[MailToken]

    def findToken(uuid: String): Future[Option[MailToken]]

    def deleteToken(uuid: String): Future[Option[MailToken]]

    def deleteExpiredTokens()
  }
}

trait MongoTokenRepositoryComponent extends TokenRepositoryComponent {
  trait MongoTokenRepository extends TokenRepository with SecureSocialDatabase with DelayedIndexOperations {
    private val indexedCollection = new IndexedCollection(
      db.collection[JSONCollection] ("tokens"),
      Seq(
        Ensure(Index(Seq("uuid" -> IndexType.Ascending), Some("uuid"), background = true)),
        Ensure(Index(Seq("expirationTime" -> IndexType.Ascending), Some("expirationTime"), background = true))
      ))

    private val collection = indexedCollection.collection

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
}

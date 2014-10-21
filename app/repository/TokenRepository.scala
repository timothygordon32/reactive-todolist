package repository

import org.joda.time.{DateTimeZone, DateTime}
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import securesocial.core.providers.MailToken
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait TokenRepository {
  def db: DB

  private lazy val collection = db.sibling("users").collection[JSONCollection]("tokens")

  implicit val dateTimeRead: Reads[DateTime] =
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }

  implicit val dateTimeWrite: Writes[DateTime] = new Writes[DateTime] {
    def writes(dateTime: DateTime): JsValue = Json.obj(
      "$date" -> dateTime.getMillis
    )
  }

  implicit val tokenFormat = Json.format[MailToken]

  def saveToken(token: MailToken): Future[MailToken] =
    collection.insert(token).map(_ => token)

  def findToken(uuid: String): Future[Option[MailToken]] =
    collection.find(Json.obj("uuid" -> uuid)).cursor[MailToken].headOption

  def deleteToken(uuid: String): Future[Option[MailToken]] = for {
    token <- findToken(uuid)
    _ <- collection.remove(Json.obj("uuid" -> uuid))
  } yield token

  def deleteExpiredTokens(): Unit = {
    val now = DateTime.now(DateTimeZone.UTC)
    collection.remove(Json.obj("expirationTime" -> Json.obj("$lt" -> now)))
  }
}

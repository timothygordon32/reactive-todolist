package repository

import models.User
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import securesocial.core._
import securesocial.core.services.SaveMode
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait ProfileRepository {
  def db: DB

  private lazy val collection = db.sibling("users").collection[JSONCollection]("profiles")

  implicit val oAuth1Format = Json.format[OAuth1Info]
  implicit val oAuth2Format = Json.format[OAuth2Info]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]
  implicit val authMethodFormat = Json.format[AuthenticationMethod]
  implicit val profileFormat = Json.format[BasicProfile]

  def save(profile: BasicProfile, mode: SaveMode): Future[User] =
    collection.save(profile).map(_ => User(profile.userId))

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] =
    collection.find(Json.obj("email" -> email)).cursor[BasicProfile].headOption
}

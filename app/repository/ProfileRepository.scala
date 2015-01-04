package repository

import models.User
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode
import time.DateTimeUtils.now

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ProfileRepository extends Indexed with SecureSocialDatabase {
  protected lazy val collection = db.collection[JSONCollection]("profiles")

  case class ProfileWithId(_id: BSONObjectID,
                           providerId: String,
                           userId: String,
                           firstName: Option[String],
                           lastName: Option[String],
                           fullName: Option[String],
                           email: Option[String],
                           avatarUrl: Option[String],
                           authMethod: AuthenticationMethod,
                           oAuth1Info: Option[OAuth1Info] = None,
                           oAuth2Info: Option[OAuth2Info] = None,
                           passwordInfo: Option[PasswordInfo] = None)

  case class UserWithAuthenticator(_id: BSONObjectID,
                                   userId: String,
                                   firstName: Option[String],
                                   authenticator: AuthenticatorDetails) {
    def toUser = User(_id, userId, firstName)
  }

  implicit def toBasicProfile(profile: ProfileWithId): BasicProfile = BasicProfile(
      profile.providerId,
      profile.userId,
      profile.firstName,
      profile.lastName,
      profile.fullName,
      profile.email,
      profile.avatarUrl,
      profile.authMethod,
      profile.oAuth1Info,
      profile.oAuth2Info,
      profile.passwordInfo)

  implicit val oAuth1Format = Json.format[OAuth1Info]
  implicit val oAuth2Format = Json.format[OAuth2Info]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]
  implicit val authMethodFormat = Json.format[AuthenticationMethod]
  implicit val profileFormat = Json.format[BasicProfile]
  implicit val bsonObjectIdFormat = BSONFormats.BSONObjectIDFormat
  implicit val profileWithIdFormat = Json.format[ProfileWithId]
  // TODO Fix over-use of date-time format
  implicit val userAuthenticatorFormat = {
    implicit val dateTimeFormat = Formats.dateTimeFormat
    Json.format[AuthenticatorDetails]
  }
  // TODO Fix over-use of date-time format
  implicit val userWithAuthenticatorReads = {
    implicit val dateTimeFormat = Formats.dateTimeFormat
    Json.reads[UserWithAuthenticator]
  }

  private val emailIndexCreated =
    ensureIndex(collection, Index(Seq("email" -> IndexType.Ascending), Some("email")))
  private val providerIdUserIdIndexCreated =
    ensureIndex(collection, Index(Seq("userId" -> IndexType.Ascending, "providerId" -> IndexType.Ascending), Some("userIdProviderId")))
  private val authenticatorIdIndexCreated =
    ensureIndex(collection, Index(Seq("authenticator.id" -> IndexType.Ascending), Some("authenticatorId")))

  implicit def toUser(profile: ProfileWithId): User = User(profile._id, profile.userId, profile.firstName)

  def indexes(): Future[Seq[Index]] = for {
    _ <- emailIndexCreated
    _ <- providerIdUserIdIndexCreated
    _ <- authenticatorIdIndexCreated
    indexes <- collection.indexesManager.list()
  } yield indexes

  def save(profile: BasicProfile, mode: SaveMode): Future[User] = mode match {
    case SaveMode.SignUp =>
      collection.update(Json.obj("userId" -> profile.userId), profile, upsert = true).flatMap { _ =>
        findKnown(profile.userId)
      }
    case SaveMode.LoggedIn =>
      findKnown(profile.userId)
    case SaveMode.PasswordChange =>
      updatePasswordInfo(profile.userId, profile.passwordInfo.get).flatMap { _ =>
        findKnown(profile.userId)
      }
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] =
    collection.find(Json.obj("email" -> email)).cursor[BasicProfile].headOption

  def findKnown(userId: String) = findProfileWithId(UsernamePasswordProvider.UsernamePassword, userId)
    .map(_.map(toUser).getOrElse(throw new IllegalStateException(s"userId [$userId] should be known to the system")))

  def findProfileWithId(providerId: String, userId: String): Future[Option[ProfileWithId]] =
    collection.find(Json.obj("providerId" -> providerId, "userId" -> userId)).cursor[ProfileWithId].headOption

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] =
    findProfileWithId(providerId, userId).map(_.map(toBasicProfile))

  def passwordInfoFor(user: User): Future[Option[PasswordInfo]] =
    find(UsernamePasswordProvider.UsernamePassword, user.username).map(_.flatMap(_.passwordInfo))

  def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] =
    updatePasswordInfo(user.username, info)

  def link(current: User, to: BasicProfile): Future[User] = Future.successful(current)

  private def updatePasswordInfo(userId: String, info: PasswordInfo): Future[Option[BasicProfile]] = {
    import BSONFormats._
    db.command(FindAndModify(
      collection.name,
      Json.obj("providerId" -> UsernamePasswordProvider.UsernamePassword, "userId" -> userId).as[BSONDocument],
      Update(Json.obj("$set" -> Json.obj("passwordInfo" -> info)).as[BSONDocument], fetchNewObject = true)))
      .map(_.map(Json.toJson(_).as[BasicProfile]))
  }

  def delete(profile: BasicProfile): Future[Int] =
    collection.remove(Json.obj("userId" -> profile.userId)).map {
      lastError => lastError.updated
    }

  // TODO Get rid of userId
  def saveAuthenticator(userId: String, userAuthenticator: AuthenticatorDetails): Future[AuthenticatorDetails] = {
    collection.update(
      Json.obj("userId" -> userId),
      Json.obj("$set" -> Json.obj("authenticator" -> userAuthenticator))
    ) map { lastError =>
      if (lastError.updatedExisting) userAuthenticator
      else throw new IllegalStateException(s"Could not find user with userId $userId")
    }
  }

  // TODO Too dense - clarify
  def findAuthenticator(id: String): Future[Option[UserAuthenticatorDetails]] = {
    // TODO Fix over-use of date-time format
    implicit val dateTimeFormat = Formats.dateTimeFormat
    val selector = Json.obj("authenticator.id" -> id, "authenticator.expirationDate" -> Json.obj("$gte" -> now))
    collection.find(selector).cursor[UserWithAuthenticator].headOption.map(_.map { userWithAuthenticator =>
      UserAuthenticatorDetails(userWithAuthenticator.toUser, userWithAuthenticator.authenticator)
    })
  }

  def deleteAuthenticator(id: String): Future[Unit] = for {
    _ <- collection.update(Json.obj("authenticator.id" -> id), Json.obj("$unset" -> Json.obj("authenticator" -> -1)))
  } yield ()
}

case class AuthenticatorDetails(id: String, expirationDate: DateTime, lastUsed: DateTime, creationDate: DateTime)

case class UserAuthenticatorDetails(user: User, authenticatorDetails: AuthenticatorDetails)


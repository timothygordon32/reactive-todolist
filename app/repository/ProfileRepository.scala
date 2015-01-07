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

trait ProfileRepository extends SecureSocialDatabase {
  private lazy val indexedCollection = new Indexed {
    val collection = db.collection[JSONCollection]("profiles")

    private val emailIndexEnsured = ensureIndex(
      collection,
      Index(Seq("email" -> IndexType.Ascending), Some("email")))
    private val authenticatorIdIndexEnsured = ensureIndex(
      collection,
      Index(Seq("authenticator.id" -> IndexType.Ascending), Some("authenticatorId")))
    private val providerIdUserIdIndexDropped = dropIndex(
      collection,
      Index(Seq("userId" -> IndexType.Ascending, "providerId" -> IndexType.Ascending), Some("userIdProviderId")))
    private val userIdIndexEnsured = ensureIndex(
      collection,
      Index(Seq("userId" -> IndexType.Ascending), Some("userId"), unique = true, background = true))

    def indexes(): Future[Seq[Index]] = for {
      _ <- emailIndexEnsured
      _ <- providerIdUserIdIndexDropped
      _ <- userIdIndexEnsured
      _ <- authenticatorIdIndexEnsured
      indexes <- collection.indexesManager.list()
    } yield indexes
  }

  private lazy val collection = indexedCollection.collection

  def indexes() = indexedCollection.indexes()

  case class IdentifiedProfile(_id: BSONObjectID,
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
                               passwordInfo: Option[PasswordInfo] = None,
                               authenticator: Option[AuthenticatorDetails]) {

    def toUser = User(_id, userId, firstName)
  }

  implicit def toBasicProfile(profile: IdentifiedProfile): BasicProfile = BasicProfile(
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

  private implicit val bsonObjectIdFormat = BSONFormats.BSONObjectIDFormat
  private implicit val dateTimeFormat = Formats.dateTimeFormat
  private implicit val oAuth1Format = Json.format[OAuth1Info]
  private implicit val oAuth2Format = Json.format[OAuth2Info]
  private implicit val passwordInfoFormat = Json.format[PasswordInfo]
  private implicit val authMethodFormat = Json.format[AuthenticationMethod]
  private implicit val profileFormat = Json.format[BasicProfile]
  private implicit val authenticatorDetailsFormat = Json.format[AuthenticatorDetails]
  private implicit val profileWithIdFormat = Json.format[IdentifiedProfile]

  implicit def toUser(profile: IdentifiedProfile): User =
    User(profile._id, profile.userId, profile.firstName)

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

  def findProfileWithId(providerId: String, userId: String): Future[Option[IdentifiedProfile]] =
    collection.find(Json.obj("providerId" -> providerId, "userId" -> userId))
      .cursor[IdentifiedProfile].headOption

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] =
    findProfileWithId(providerId, userId).map(_.map(toBasicProfile))

  def passwordInfoFor(user: User): Future[Option[PasswordInfo]] =
    find(UsernamePasswordProvider.UsernamePassword, user.username).map(_.flatMap(_.passwordInfo))

  def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] =
    updatePasswordInfo(user.username, info)

  def link(current: User, to: BasicProfile): Future[User] = Future.successful(current)

  private def updatePasswordInfo(userId: String, info: PasswordInfo): Future[Option[BasicProfile]] = {
    import play.modules.reactivemongo.json.BSONFormats._
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

  def saveAuthenticator(user: User, userAuthenticator: AuthenticatorDetails): Future[AuthenticatorDetails] = {
    collection.update(
      Json.obj("userId" -> user.username),
      Json.obj("$set" -> Json.obj("authenticator" -> userAuthenticator))
    ) map { lastError =>
      if (lastError.updatedExisting) userAuthenticator
      else throw new IllegalStateException(s"Could not find user with userId ${user.username}")
    }
  }

  def findAuthenticator(id: String): Future[Option[UserAuthenticatorDetails]] = {
    val selector = Json.obj("authenticator.id" -> id, "authenticator.expirationDate" -> Json.obj("$gte" -> now))
    collection.find(selector).cursor[IdentifiedProfile].headOption.map(_.map { userWithAuthenticator =>
      UserAuthenticatorDetails(userWithAuthenticator.toUser, userWithAuthenticator.authenticator.get)
    })
  }

  def deleteAuthenticator(id: String): Future[Unit] = for {
    _ <- collection.update(Json.obj("authenticator.id" -> id), Json.obj("$unset" -> Json.obj("authenticator" -> -1)))
  } yield ()
}

case class AuthenticatorDetails(id: String, expirationDate: DateTime, lastUsed: DateTime, creationDate: DateTime)

case class UserAuthenticatorDetails(user: User, authenticatorDetails: AuthenticatorDetails)

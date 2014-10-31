package repository

import models.User
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import reactivemongo.api.indexes.{CollectionIndexesManager, IndexType, Index}
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.{Update, FindAndModify}
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ProfileRepository extends Indexed with SecureSocialDatabase {
  def db: DB

  private lazy val collection = db.sibling(secureSocialDatabase).collection[JSONCollection]("profiles")

  implicit val oAuth1Format = Json.format[OAuth1Info]
  implicit val oAuth2Format = Json.format[OAuth2Info]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]
  implicit val authMethodFormat = Json.format[AuthenticationMethod]
  implicit val profileFormat = Json.format[BasicProfile]

  override def indexesManager: CollectionIndexesManager = collection.indexesManager

  private val emailIndexCreated =
    ensureIndex(Index(Seq("email" -> IndexType.Ascending), Some("email")))
  private val providerIdUserIdIndexCreated =
    ensureIndex(Index(Seq("userId" -> IndexType.Ascending, "providerId" -> IndexType.Ascending), Some("providerIdUserId")))

  def indexes(): Future[List[Index]] = for {
    _ <- emailIndexCreated
    _ <- providerIdUserIdIndexCreated
    indexes <- collection.indexesManager.list()
  } yield indexes

  def save(profile: BasicProfile, mode: SaveMode): Future[User] = mode match {
    case SaveMode.SignUp => collection.save(profile).map(_ => User(profile.userId))
    case SaveMode.LoggedIn => Future.successful(User(profile.userId))
    case SaveMode.PasswordChange => updatePasswordInfo(profile.userId, profile.passwordInfo.get).map(_ => User(profile.userId))
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] =
    collection.find(Json.obj("email" -> email)).cursor[BasicProfile].headOption

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] =
    collection.find(Json.obj("providerId" -> providerId, "userId" -> userId)).cursor[BasicProfile].headOption

  def passwordInfoFor(user: User): Future[Option[PasswordInfo]] =
    find(UsernamePasswordProvider.UsernamePassword, user.username).map(_.flatMap(_.passwordInfo))

  def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] =
    updatePasswordInfo(user.username, info)

  def link(current: User, to: BasicProfile): Future[User] = Future.successful(current)

  private def updatePasswordInfo(userId: String, info: PasswordInfo): Future[Option[BasicProfile]] = {
    import BSONFormats._
    db.sibling(secureSocialDatabase).command(FindAndModify(
      collection.name,
      Json.obj("providerId" -> UsernamePasswordProvider.UsernamePassword, "userId" -> userId).as[BSONDocument],
      Update(Json.obj("$set" -> Json.obj("passwordInfo" -> info)).as[BSONDocument], fetchNewObject = true)))
      .map(_.map(Json.toJson(_).as[BasicProfile]))
  }
}

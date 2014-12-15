package repository

import models.User
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.specification.Scope
import play.api.libs.json.Json
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import securesocial.core.authenticator.{Authenticator, AuthenticatorStore, CookieAuthenticator, IdGenerator}
import utils.{StartedFakeApplication, UniqueStrings}
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode

import scala.concurrent.Future
import scala.reflect.ClassTag

class ProfileRepositorySpec extends PlaySpecification with StartedFakeApplication {

  "Profile repository" should {

    "have an index on email" in new TestCase {
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("email" -> IndexType.Ascending)) must not be empty
    }

    "have an index on providerId and userId" in new TestCase {
      val indexes = await(repo.indexes())
      indexes.filter(_.key == Seq("userId" -> IndexType.Ascending, "providerId" -> IndexType.Ascending)) must not be empty
    }

    "save profile and find a user by provider and email" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.findByEmailAndProvider(email, providerId = providerId)) must be equalTo Some(profile)
    }

    "save profile and find a user by provider and user id" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.find(providerId, userId)) must be equalTo Some(profile)
    }

    "save profile and find user password info" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.passwordInfoFor(User(id, userId, firstName))) must be equalTo profile.passwordInfo
    }

    "update a password for a user" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp))
      val user = await(repo.findKnown(profile.userId))

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.updatePasswordInfo(user, changedInfo)).get.passwordInfo must be equalTo Some(changedInfo)

      await(repo.passwordInfoFor(user)) must be equalTo Some(changedInfo)
    }

    "sign in the user" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId

      await(repo.save(profile, SaveMode.LoggedIn)).username must be equalTo userId
    }

    "change the user password" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)).username must be equalTo userId
      val user = await(repo.findKnown(profile.userId))

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.save(profile.copy(passwordInfo = Some(changedInfo)), SaveMode.PasswordChange)).username must be equalTo userId

      await(repo.passwordInfoFor(user)) must be equalTo Some(changedInfo)
    }

    "link the user to the profile" in new TestCase {
      val user = User(id, userId, firstName)
      await(repo.link(user, profile)) must be equalTo user
    }

    "find an authenticator" in new TestCase {
      val store = new UserProfileAuthenticatorStore[CookieAuthenticator[User]] {}
      val user = await(store.save(profile, SaveMode.SignUp))
      val created = DateTime.now(DateTimeZone.UTC)
      val expire = created.plusHours(1)
      val lastUsed = created.plusMillis(1)
      val authenticatorId = await(new IdGenerator.Default().generate)
      await(store.save(CookieAuthenticator(authenticatorId, user, expire, lastUsed, created, store), 3600))

      val authenticator = await(store.find(authenticatorId)).get

      authenticator.user must be equalTo user
      authenticator.expirationDate must be equalTo expire
      authenticator.lastUsed must be equalTo lastUsed
      authenticator.creationDate must be equalTo created
    }
  }

  trait UserProfileAuthenticatorStore[A <: Authenticator[User]] extends AuthenticatorStore[A] with ProfileRepository {

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

    implicit val userAuthenticatorFormat = {
      implicit val dateTimeFormat = Formats.dateTimeFormat
      Json.format[UserAuthenticator]
    }

    implicit val userWithAuthenticatorReads = Json.reads[UserWithAuthenticator]

    override def find(id: String)(implicit ct: ClassTag[A]): Future[Option[A]] = {
      collection.find(Json.obj("authenticator.id" -> id)).cursor[UserWithAuthenticator].headOption.map(_.map {
        userWithAuthenticator =>
          CookieAuthenticator(
            id,
            userWithAuthenticator.toUser,
            userWithAuthenticator.authenticator.expirationDate,
            userWithAuthenticator.authenticator.lastUsed,
            userWithAuthenticator.authenticator.creationDate,
            this.asInstanceOf[AuthenticatorStore[CookieAuthenticator[User]]]).asInstanceOf[A]
      })
    }

    override def delete(id: String): Future[Unit] = ???

    override def save(authenticator: A, timeoutInSeconds: Int): Future[A] = {
      val userId = authenticator.user.username
      collection.update(
        Json.obj("userId" -> userId),
        Json.obj("$set" -> Json.obj("authenticator" -> userAuthenticator(authenticator)))
      ) map { lastError =>
        if (lastError.updatedExisting) authenticator
        else throw new IllegalStateException(s"Could not find user with userId $userId")
      }
    }
  }

  trait TestCase extends Scope with UniqueStrings {
    lazy val repo = new ProfileRepository {}
    val id = BSONObjectID.generate
    val userId = uniqueString
    val firstName = Some(s"Joe-$userId")
    val providerId = UsernamePasswordProvider.UsernamePassword
    val email = s"$userId@somemail.com"
    val profile = BasicProfile(
      providerId = providerId,
      userId = userId,
      firstName = firstName,
      lastName = Some("Bloggs"),
      fullName = Some("Joe Blow Bloggs"),
      email = Some(email),
      avatarUrl = Some("http://2.gravatar.com/avatar/5f6b0d7f7c102038f2b367dbc797c736"),
      authMethod = AuthenticationMethod.UserPassword,
      oAuth1Info = Some(OAuth1Info(token = "oAuth1Token", secret = "oAuth1Secret")),
      oAuth2Info = Some(OAuth2Info(accessToken = "oAuth2Token", tokenType = Some("oAuth2TokenType"), expiresIn = Some(999), refreshToken = Some("refreshToken"))),
      passwordInfo = Some(PasswordInfo(hasher = "bcrypt", password = "password", salt = Some("salt")))
    )
  }
}

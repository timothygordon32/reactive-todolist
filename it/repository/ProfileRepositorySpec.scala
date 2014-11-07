package repository

import java.util.UUID

import models.User
import org.specs2.specification.Scope
import play.api.test.PlaySpecification
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.indexes.IndexType
import utils.StartedFakeApplication
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode

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
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.findByEmailAndProvider(email, providerId = providerId)) must be equalTo Some(profile)
    }

    "save profile and find a user by provider and user id" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.find(providerId, userId)) must be equalTo Some(profile)
    }

    "save profile and find user password info" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.passwordInfoFor(User(userId))) must be equalTo profile.passwordInfo
    }

    "update a password for a user" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.updatePasswordInfo(User(userId), changedInfo)).get.passwordInfo must be equalTo Some(changedInfo)

      await(repo.passwordInfoFor(User(userId))) must be equalTo Some(changedInfo)
    }

    "sign in the user" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.save(profile, SaveMode.LoggedIn)) must be equalTo User(userId)
    }

    "change the user password" in new TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.save(profile.copy(passwordInfo = Some(changedInfo)), SaveMode.PasswordChange)) must be equalTo User(userId)

      await(repo.passwordInfoFor(User(userId))) must be equalTo Some(changedInfo)
    }

    "link the user to the profile" in new TestCase {
      await(repo.link(User(userId), profile)) must be equalTo User(userId)
    }
  }

  trait TestCase extends Scope {
    lazy val repo = new ProfileRepository {}
    val userId = UUID.randomUUID.toString
    val providerId = UsernamePasswordProvider.UsernamePassword
    val email = s"$userId@somemail.com"
    val profile = BasicProfile(
      providerId = providerId,
      userId = userId,
      firstName = Some("Joe"),
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

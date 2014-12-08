package repository

import models.User
import org.specs2.specification.Scope
import play.api.test.PlaySpecification
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import utils.{StartedFakeApplication, UniqueStrings}
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

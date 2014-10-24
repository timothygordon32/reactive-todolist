package repository

import java.util.UUID

import models.User
import play.api.test.{PlaySpecification, WithApplication}
import play.modules.reactivemongo.ReactiveMongoPlugin
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode

class ProfileRepositorySpec extends PlaySpecification {

  "Profile repository" should {

    "save profile and find a user" in new WithApplication with TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.findByEmailAndProvider(email, providerId = providerId)) must be equalTo Some(profile)

      await(repo.find(providerId, userId)) must be equalTo Some(profile)

      await(repo.passwordInfoFor(User(userId))) must be equalTo profile.passwordInfo
    }

    "update a password for a user" in new WithApplication with TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.updatePasswordInfo(User(userId), changedInfo)).get.passwordInfo must be equalTo Some(changedInfo)

      await(repo.passwordInfoFor(User(userId))) must be equalTo Some(changedInfo)
    }

    "sign in the user" in new WithApplication with TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.save(profile, SaveMode.LoggedIn)) must be equalTo User(userId)
    }

    "change the user password" in new WithApplication with TestCase {
      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      val changedInfo = PasswordInfo(hasher = "bcrypt", password = "changedPassword", salt = Some("salt"))

      await(repo.save(profile.copy(passwordInfo = Some(changedInfo)), SaveMode.PasswordChange)) must be equalTo User(userId)

      await(repo.passwordInfoFor(User(userId))) must be equalTo Some(changedInfo)
    }

    "link the user to the profile" in new WithApplication with TestCase {
      await(repo.link(User(userId), profile)) must be equalTo User(userId)
    }
  }

  trait TestCase {
    import play.api.Play.current
    val repo = new ProfileRepository {
      override def db = ReactiveMongoPlugin.db
    }
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

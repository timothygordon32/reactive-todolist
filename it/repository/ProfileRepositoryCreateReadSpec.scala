package repository

import java.util.UUID

import models.User
import play.api.test.{PlaySpecification, WithApplication}
import play.modules.reactivemongo.ReactiveMongoPlugin
import securesocial.core._
import securesocial.core.services.SaveMode

class ProfileRepositoryCreateReadSpec extends PlaySpecification {

  "Profile repository" should {

    "save profile and find a user" in new WithApplication {
      val repo = new ProfileRepository {
        override def db = ReactiveMongoPlugin.db
      }
      val userId = UUID.randomUUID.toString
      val email = s"$userId@somemail.com"
      val profile = BasicProfile(
        providerId = "userpass",
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

      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId)

      await(repo.findByEmailAndProvider(email, providerId = "userpass")) must be equalTo Some(profile)
    }
  }
}

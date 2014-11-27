package migration

import java.util.UUID

import org.specs2.specification.Scope
import repository.ProfileRepository
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.providers.utils.PasswordHasher
import security.PasswordHash

trait MigrationTestCase extends Scope {
  lazy val profileRepository = new ProfileRepository {}

  val ProviderId = UsernamePasswordProvider.UsernamePassword

  def email(userId: String) = s"$userId@somemail.com"

  def createProfile(userId: String): BasicProfile = {
    BasicProfile(
      providerId = ProviderId,
      userId = userId,
      firstName = Some(s"Joe-$userId"),
      lastName = Some("Bloggs"),
      fullName = Some("Joe Blow Bloggs"),
      email = Some(email(userId)),
      avatarUrl = Some("http://2.gravatar.com/avatar/5f6b0d7f7c102038f2b367dbc797c736"),
      authMethod = AuthenticationMethod.UserPassword,
      oAuth1Info = Some(OAuth1Info(token = "oAuth1Token", secret = "oAuth1Secret")),
      oAuth2Info = Some(OAuth2Info(accessToken = "oAuth2Token", tokenType = Some("oAuth2TokenType"), expiresIn = Some(999), refreshToken = Some("refreshToken"))),
      passwordInfo = Some(PasswordInfo(PasswordHasher.id, PasswordHash.hash(password)))
    )
  }

  val userId1 = UUID.randomUUID.toString
  val email1 = email(userId1)
  val password = "secret1"
  val profile1 = createProfile(userId1)

  val userId2 = UUID.randomUUID.toString
  val email2 = email(userId2)
  val profile2 = createProfile(userId2)
}
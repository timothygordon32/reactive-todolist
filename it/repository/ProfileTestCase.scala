package repository

import org.specs2.specification.Scope
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import reactivemongo.bson.BSONObjectID
import securesocial.core.authenticator.IdGenerator
import utils.UniqueStrings
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider

trait ProfileTestCase
  extends Scope
  with UniqueStrings
  with FutureAwaits
  with DefaultAwaitTimeout {

  lazy val profileRepository: ProfileRepository = new MongoProfileRepository {}
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

  def generateAuthenticatorId: String = await(new IdGenerator.Default().generate)

  val authenticatorId = generateAuthenticatorId
}

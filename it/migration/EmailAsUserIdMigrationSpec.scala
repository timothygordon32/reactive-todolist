package migration

import java.util.UUID

import org.specs2.specification.Scope
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.test._
import repository.ProfileRepository
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.SaveMode
import security.{MongoUserService, PasswordHash}
import utils.StartedFakeApplication
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider

class EmailAsUserIdMigrationSpec extends PlaySpecification with StartedFakeApplication {

  "Old profiles" should {

    "be migrated automatically" in new TestCase {
      // Given
      await(repo.save(profile1, SaveMode.SignUp)).username must be equalTo userId1
      await(repo.save(profile2, SaveMode.SignUp)).username  must be equalTo userId2
      def includeProfile(p: BasicProfile): Boolean = Seq(userId1, userId2).contains(p.userId)
      // When
      await(MongoUserService.migrateUsernameToEmail(includeProfile))
      // Then
      eventually(await(repo.find(ProviderId, email(userId1))) must not be None)
      eventually(await(repo.find(ProviderId, userId1)) must be equalTo None)
      eventually(await(repo.find(ProviderId, email(userId2))) must not be None)
      eventually(await(repo.find(ProviderId, userId2)) must be equalTo None)
    }

    "be accessible by their email address" in new TestCase {
      await(repo.save(profile1, SaveMode.SignUp)).username must be equalTo userId1

      val oldIdentity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> userId1, "password" -> password))).get).get("id").get

      val text = "text-" + UUID.randomUUID
      await(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(oldIdentity)).get)

      val newIdentityResponse = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> email1, "password" -> password))).get
      val maybeCookie = cookies(newIdentityResponse).get("id")
      maybeCookie must not be None
      val newIdentity = maybeCookie.get

      val tasksForNewIdentity = route(FakeRequest(GET, "/tasks")
        .withCookies(newIdentity)).get
      status(tasksForNewIdentity) must be equalTo OK
      contentAsJson(tasksForNewIdentity).as[JsArray].value.head.as[JsObject] \ "text" must be equalTo JsString(text)
    }
  }

  trait TestCase extends Scope {
    lazy val repo = new ProfileRepository {}

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
}

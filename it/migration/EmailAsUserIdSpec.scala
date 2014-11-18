package migration

import java.util.UUID

import models.User
import org.specs2.specification.Scope
import play.api.libs.json.{JsObject, JsArray, JsString, Json}
import play.api.test._
import repository.ProfileRepository
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.SaveMode
import security.PasswordHash
import utils.StartedFakeApplication
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider

class EmailAsUserIdSpec extends PlaySpecification with StartedFakeApplication {

  "Old profiles" should {

    "be accessible by their email address" in new TestCase {

      await(repo.save(profile, SaveMode.SignUp)) must be equalTo User(userId, firstName)

      val oldIdentity = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> userId, "password" -> password))).get).get("id").get

      val text = "text-" + UUID.randomUUID
      await(route(FakeRequest(POST, "/tasks")
        .withBody(Json.obj("text" -> text, "done" -> false))
        .withCookies(oldIdentity)).get)

      val newIdentityResponse = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> email, "password" -> password))).get
      val maybeCookie = cookies(newIdentityResponse).get("id")
      maybeCookie must not be None
      val newIdentity = maybeCookie.get

      val tasksWithCreated = route(FakeRequest(GET, "/tasks")
        .withCookies(newIdentity)).get
      status(tasksWithCreated) must be equalTo OK
      contentAsJson(tasksWithCreated).as[JsArray].value.head.as[JsObject] \ "text" must be equalTo JsString(text)
    }
  }

  trait TestCase extends Scope {
    lazy val repo = new ProfileRepository {}
    val userId = UUID.randomUUID.toString
    val password = "secret1"
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
      passwordInfo = Some(PasswordInfo(PasswordHasher.id, PasswordHash.hash(password)))
    )
  }
}

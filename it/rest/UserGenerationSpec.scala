package rest

import play.api.libs.json._
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits, PlaySpecification}
import repository.ProfileRepository
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.SaveMode
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}
import security.{UserGeneration, User, PasswordHash}
import utils.{StartedFakeApplication, UniqueStrings}

class UserGenerationSpec extends PlaySpecification with StartedFakeApplication with UserGeneration {

  "Security" should {

    "create a user on demand" in {

      val user = generateRegisteredUser

      val login = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> user.userId, "password" -> user.password))).get

      val id = cookies(login).get("id").head
      val userDescription = route(FakeRequest(GET, "/login").withCookies(id)).get
      status(userDescription) must equalTo(OK)
      contentAsJson(userDescription) \ "username" must be equalTo JsString(user.userId)
    }
  }
}


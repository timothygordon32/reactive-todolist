package rest

import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test._
import ui.model.Users
import utils.StartedFakeApplication

class SecurityRestApiSpec extends PlaySpecification with StartedFakeApplication with Users {

  "Security REST API" should {

    "validate the user credentials and use the user token" in {
      val user = generateRegisteredUser

      val login = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> user.userId, "password" -> user.password))).get

      status(login) must equalTo(SEE_OTHER)
      val id = cookies(login).get("id")
      id must not be None

      val userDetails = route(FakeRequest(GET, "/login").withCookies(id.get)).get

      status(userDetails) must equalTo(OK)
      contentAsJson(userDetails) must equalTo(Json.obj("username" -> user.userId, "firstName" -> user.firstName))
    }

    "reject the user" in {
      val invalidLogin = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> User1.userId, "password" -> "wrong"))).get

      status(invalidLogin) must equalTo(BAD_REQUEST)
    }

    "deny without user token" in {
      // Given
      val invalid: Cookie = Cookie("id", "invalid")
      // When
      val user = route(FakeRequest(GET, "/login").withCookies(invalid)).get
      // Then
      status(user) must equalTo(UNAUTHORIZED)
    }

    "log the user off" in {
      // Given
      val user = generateRegisteredUser
      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> user.userId, "password" -> user.password))).get).get("id").head
      // And
      val identity = route(FakeRequest(GET, "/login").withCookies(id)).get
      status(identity) must equalTo(OK)
      // When
      val logoff = route(FakeRequest(DELETE, "/login").withCookies(id)).get
      // Then
      status(logoff) must equalTo(NO_CONTENT)
    }
  }
}

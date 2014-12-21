package rest

import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test._
import utils.StartedFakeApplication

class SecurityRestApiSpec extends PlaySpecification with StartedFakeApplication {

  lazy val login = route(FakeRequest(POST, "/users/authenticate/userpass")
    .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "!secret1"))).get

  lazy val id = cookies(login).get("id").head

  "Security REST API" should {

    "validate the user credentials" in {
      status(login) must equalTo(SEE_OTHER)
      cookies(login).get("id") must not be None
    }

    "reject the user" in {
      val invalidLogin = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "wrong"))).get

      status(invalidLogin) must equalTo(BAD_REQUEST)
    }

    "validate the user token" in {
      val user = route(FakeRequest(GET, "/login").withCookies(id)).get

      status(user) must equalTo(OK)
      contentAsJson(user) must equalTo(Json.obj("username" -> "testuser1@nomail.com", "firstName" -> "Test1"))
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
      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser3@nomail.com", "password" -> "!secret3"))).get).get("id").head
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

package rest

import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test._
import utils.StartedFakeApplication

class SecurityRestApiSpec extends PlaySpecification with StartedFakeApplication {

  "Security REST API" should {

    "validate the user credentials" in {

      val login = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "!secret1"))).get

      status(login) must equalTo(SEE_OTHER)
      cookies(login).get("id") must not be None
    }

    "reject the user" in {

      val login = route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "wrong"))).get

      status(login) must equalTo(BAD_REQUEST)
    }

    "validate the user token" in {

      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "!secret1"))).get).get("id").head

      val login = route(FakeRequest(GET, "/login").withCookies(id)).get

      status(login) must equalTo(OK)
      contentAsJson(login) must equalTo(Json.obj("username" -> "testuser1@nomail.com", "firstName" -> "Test1"))
    }

    "deny without user token" in {
      // Given
      val invalid: Cookie = Cookie("id", "invalid")
      // When
      val login = route(FakeRequest(GET, "/login").withCookies(invalid)).get
      // Then
      status(login) must equalTo(UNAUTHORIZED)
    }

    "log the user off" in {
      // Given
      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass")
        .withBody(Json.obj("username" -> "testuser1@nomail.com", "password" -> "!secret1"))).get).get("id").head
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

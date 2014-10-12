package rest

import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test._

class SecurityRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "validate the user credentials" in new WithApplication {

      val login = route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get

      status(login) must equalTo(SEE_OTHER)
      cookies(login).get("id") must not be None
    }

    "reject the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "wrong"))).get

      status(login) must equalTo(BAD_REQUEST)
    }

    "validate the user token" in new WithApplication {

      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get).get("id").head

      val login = route(FakeRequest(GET, "/login").withCookies(id)).get

      status(login) must equalTo(OK)
      contentAsJson(login) must equalTo(Json.obj("username" -> "testuser"))
    }

    "deny without user token" in new WithApplication() {
      // Given
      val invalid: Cookie = Cookie("id", "invalid")
      // When
      val login = route(FakeRequest(GET, "/login").withCookies(invalid)).get
      // Then
      status(login) must equalTo(UNAUTHORIZED)
    }

    "log the user off" in new WithApplication() {
      // Given
      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get).get("id").head
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

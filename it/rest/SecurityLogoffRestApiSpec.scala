package rest

import play.api.libs.json.Json
import play.api.test._

class SecurityLogoffRestApiSpec extends PlaySpecification {

  "Security REST API" should {

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

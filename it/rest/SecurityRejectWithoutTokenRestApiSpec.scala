package rest

import play.api.mvc.Cookie
import play.api.test._

class SecurityRejectWithoutTokenRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "deny without user token" in new WithApplication() {
      // Given
      val invalid: Cookie = Cookie("id", "invalid")
      // When
      val login = route(FakeRequest(GET, "/login").withCookies(invalid)).get
      // Then
      status(login) must equalTo(UNAUTHORIZED)
    }
  }
}

package rest

import play.api.test._

class SecurityRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "validate the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "testuser", "password" -> "secret")).get

      status(login) must equalTo(OK)
    }

    "reject the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "testuser", "password" -> "wrong")).get

      status(login) must equalTo(UNAUTHORIZED)
    }
  }
}

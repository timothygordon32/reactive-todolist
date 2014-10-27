package rest

import play.api.libs.json.Json
import play.api.test._

class SecurityValidateUserCredentialsRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "validate the user credentials" in new WithApplication {

      val login = route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get

      status(login) must equalTo(SEE_OTHER)
      cookies(login).get("id") must not be None
    }
  }
}

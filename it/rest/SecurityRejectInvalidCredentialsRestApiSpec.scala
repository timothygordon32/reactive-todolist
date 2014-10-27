package rest

import play.api.libs.json.Json
import play.api.test._

class SecurityRejectInvalidCredentialsRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "reject the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "wrong"))).get

      status(login) must equalTo(BAD_REQUEST)
    }
  }
}

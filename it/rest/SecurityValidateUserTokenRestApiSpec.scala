package rest

import play.api.libs.json.Json
import play.api.test._

class SecurityValidateUserTokenRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "validate the user token" in new WithApplication {

      val id = cookies(route(FakeRequest(POST, "/users/authenticate/userpass").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get).get("id").head

      val login = route(FakeRequest(GET, "/login").withCookies(id)).get

      status(login) must equalTo(OK)
      contentAsJson(login) must equalTo(Json.obj("username" -> "testuser"))
    }
  }
}

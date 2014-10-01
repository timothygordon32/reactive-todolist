package rest

import play.api.libs.json.Json
import play.api.test._

class SecurityRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "validate the user credentials" in new WithApplication {

      val login = route(FakeRequest(POST, "/login").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get

      status(login) must equalTo(OK)
      contentAsJson(login) must equalTo(Json.obj("username" -> "testuser"))
    }

    "reject the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/login").withBody(Json.obj("username" -> "testuser", "password" -> "wrong"))).get

      status(login) must equalTo(UNAUTHORIZED)
    }

    "validate the user token" in new WithApplication {

      val login = route(FakeRequest(GET, "/login").withSession("username" -> "testuser")).get

      status(login) must equalTo(OK)
      contentAsJson(login) must equalTo(Json.obj("username" -> "testuser"))
    }

    "deny without user token" in new WithApplication() {
      // Given
      val emptySession: Seq[(String, String)] = Seq.empty
      // When
      val login = route(FakeRequest(GET, "/login").withSession(emptySession:_ *)).get
      // Then
      status(login) must equalTo(UNAUTHORIZED)
    }

    "log the user off" in new WithApplication() {
      // Given
      val login = route(FakeRequest(POST, "/login").withBody(Json.obj("username" -> "testuser", "password" -> "secret"))).get
      status(login) must equalTo(OK)
      // And
      val identity = route(FakeRequest(GET, "/login").withSession("username" -> "testuser")).get
      status(identity) must equalTo(OK)
      // When
      val logoff = route(FakeRequest(DELETE, "/login").withSession("username" -> "testuser")).get
      // Then
      status(logoff) must equalTo(OK)
      session(logoff).get("username") must equalTo(None)
    }
  }
}

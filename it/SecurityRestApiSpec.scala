
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class SecurityRestApiSpec extends PlaySpecification {

  "Security REST API" should {

    "validate the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "bob", "password" -> "secret")).get

      status(login) must equalTo(OK)
    }

    "reject the user" in new WithApplication {

      val login = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("username" -> "bob", "password" -> "wrong")).get

      status(login) must equalTo(UNAUTHORIZED)
    }
  }
}

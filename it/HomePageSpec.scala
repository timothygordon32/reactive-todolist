
import play.api.test._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class HomePageSpec extends PlaySpecification {

  "Application" should {

    "work from within a browser" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX)) {

      browser.goTo("http://localhost:" + port)

      browser.pageSource must contain("Todo list")
    }
  }
}

package ui

import java.util.UUID

import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}

class UiSpec extends PlaySpecification {

  "Application" should {

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX)) {

      var label = s"label-${UUID.randomUUID}"

      await(WS.url(s"http://localhost:$port/json/tasks").post(Json.obj("label" -> label)))

      val page = browser.goTo(s"http://localhost:$port/")

      page.await until "input" hasName "username"

      page.fill("#username").`with`("testuser")
      page.fill("#password").`with`("secret")

      page.find("#login").click()

      page.await until "#headline" containsText "testuser"
      page.await until "ul li label span" hasText label
    }
  }

}

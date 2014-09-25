package ui

import java.util.UUID
import java.util.concurrent.TimeUnit

import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}

class UiSpec extends PlaySpecification {

  "Application" should {

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX)) {

      var label = s"label-${UUID.randomUUID}"

      val page = browser.goTo(s"http://localhost:$port/")

      (page.await atMost(5, TimeUnit.SECONDS) until "input").isPresent

      page.fill("#username").`with`("testuser")
      page.fill("#password").`with`("secret")

      page.find("#login").click()

      page.await atMost(5, TimeUnit.SECONDS) until "#headline" containsText "testuser"

      page.fill("#add-task-text").`with`(label)
      page.find("#add-task").click()

      page.await atMost(5, TimeUnit.SECONDS) until "ul li label span" hasText label
    }
  }
}

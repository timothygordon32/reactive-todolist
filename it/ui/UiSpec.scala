package ui

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.fluentlenium.core.filter.FilterConstructor._
import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}

class UiSpec extends PlaySpecification {

  "Application" should {

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX)) {
      val page = browser.goTo(s"http://localhost:$port/")

      (page.await atMost(5, TimeUnit.SECONDS) until "input").isPresent

      page.fill("#username").`with`("testuser")
      page.fill("#password").`with`("secret")

      page.find("#login").click()

      page.await atMost(5, TimeUnit.SECONDS) until "#headline" containsText "testuser"

      var text = s"text-${UUID.randomUUID}"

      page.fill("#add-task-text").`with`(text)
      page.find("#add-task").click()

      page.await atMost(5, TimeUnit.SECONDS) until "ul li label span" hasText text

      page.find("ul li label", withText(text)).find("input").click()

      page.await until "ul li label span.done-true" hasText text

      val refreshedPage = browser.goTo(s"http://localhost:$port/#tasks")
      refreshedPage.await atMost(5, TimeUnit.SECONDS) until "ul li label span.done-true" hasText text
    }
  }
}

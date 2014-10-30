package ui

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.fluentlenium.core.filter.FilterConstructor._
import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}

class UiSpec extends PlaySpecification {

  "Application" should {

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX)) {
      val page = browser.goTo(s"http://localhost:$port/")

      (page.await atMost(5, TimeUnit.SECONDS) until "#username").isPresent

      page.fill("#username").`with`("testuser1")
      page.fill("#password").`with`("secret1")

      page.find("#login").click()

      page.await atMost(5, TimeUnit.SECONDS) until "#headline" containsText "testuser1"

      var text = s"text-${UUID.randomUUID}"

      page.fill("#add-task-text").`with`(text)
      page.find("#add-task").click()

      page.await atMost(5, TimeUnit.SECONDS) until ".list-group-item span" hasText text

      page.find(".list-group-item", withText(text)).find("input").click()

      page.await until ".list-group-item span.done-true" hasText text

      val refreshedPage = browser.goTo(s"http://localhost:$port/#tasks")
      refreshedPage.await atMost(5, TimeUnit.SECONDS) until ".list-group-item span.done-true" hasText text

      refreshedPage.find("#clear-completed").click()
      (page.await atMost(5, TimeUnit.SECONDS) until ".list-group-item span.done-true" withText text).isNotPresent
    }
  }
}

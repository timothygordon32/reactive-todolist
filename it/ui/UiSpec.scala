package ui

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.fluentlenium.core.filter.FilterConstructor._
import org.openqa.selenium.WebDriver
import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}

class UiSpec extends PlaySpecification {

  "Application" should {

    class LoginPage(val driver: WebDriver, val port: Int) extends FluentPage(driver) {
      def login(username: String, password: String): TaskPage = {
        await untilPage this isAt()

        fill("#username").`with`(username)
        fill("#password").`with`(password)

        find("#login").click()

        val tasksPage = new TaskPage(username, driver, port)
        await untilPage tasksPage isAt()
        tasksPage
      }

      override def getUrl = s"http://localhost:$port/"

      override def isAt() = (await atMost(5, TimeUnit.SECONDS) until "#username").isPresent
    }

    class TaskPage(val username: String, val driver: WebDriver, val port: Int) extends FluentPage(driver) {
      def addTask(text: String) {
        fill("#add-task-text").`with`(text)
        find("#add-task").click()
      }

      def mustHaveTaskWithText(text: String) {
        await atMost(5, TimeUnit.SECONDS) until ".list-group-item span" hasText text
      }

      override def getUrl = s"http://localhost:$port/#/tasks"

      override def isAt() = await atMost(5, TimeUnit.SECONDS) until "#headline" containsText username
    }

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX), port = 19001) {
      val loginPage = browser.goTo(new LoginPage(webDriver, port))

      val taskPage = loginPage.login("testuser1", "secret1")

      var uniqueTaskText = s"text-${UUID.randomUUID}"

      taskPage addTask uniqueTaskText

      taskPage mustHaveTaskWithText uniqueTaskText

      taskPage.find(".list-group-item", withText(uniqueTaskText)).find("input").click()

      taskPage.await until ".list-group-item span.done-true" hasText uniqueTaskText

      val refreshedPage = browser.goTo(new TaskPage("testuser1", webDriver, port))
      refreshedPage.await atMost(5, TimeUnit.SECONDS) until ".list-group-item span.done-true" hasText uniqueTaskText

      refreshedPage.find("#clear-completed").click()
      (refreshedPage.await atMost(5, TimeUnit.SECONDS) until ".list-group-item span.done-true" withText uniqueTaskText).isNotPresent
    }
  }
}

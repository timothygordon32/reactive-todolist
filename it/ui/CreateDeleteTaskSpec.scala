package ui

import java.util.UUID

import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}

class CreateDeleteTaskSpec extends PlaySpecification {

  "Application" should {

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX), port = 19001) {
      val loginPage = browser.goTo(new LoginPage(webDriver, port))

      val taskPage = loginPage.login("testuser1", "secret1")

      var uniqueTaskText = s"text-${UUID.randomUUID}"

      taskPage addTask uniqueTaskText

      taskPage mustHaveTaskWithText uniqueTaskText

      taskPage toggleTaskWithText uniqueTaskText

      val refreshedTaskPage = taskPage.refresh
      refreshedTaskPage mustHaveTaskWithText uniqueTaskText

      refreshedTaskPage.clearCompletedTasks
      refreshedTaskPage mustNotHaveTaskWithText uniqueTaskText
    }
  }
}

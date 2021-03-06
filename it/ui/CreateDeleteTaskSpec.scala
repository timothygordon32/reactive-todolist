package ui

import java.util.UUID

import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}
import security.Users
import ui.model.LoginPageSugar
import utils.PortAllocator._

class CreateDeleteTaskSpec extends PlaySpecification with Users {

  "Task page" should {

    "allow creating a task, marking it as done and clearing it" in new WithBrowser(
      webDriver = WebDriverFactory(FIREFOX), port = appPort) with LoginPageSugar {

      val taskPage = browser goTo loginPage login User1

      var taskText = s"text-${UUID.randomUUID}"

      taskPage addTask taskText

      taskPage mustHaveTaskWithText taskText

      taskPage toggleTaskWithText taskText

      val refreshedTaskPage = taskPage.refresh
      refreshedTaskPage mustHaveTaskWithText taskText

      refreshedTaskPage.clearCompletedTasks
      refreshedTaskPage mustNotHaveTaskWithText taskText
    }
  }
}

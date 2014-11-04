package ui

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.fluentlenium.core.filter.FilterConstructor._
import org.openqa.selenium.WebDriver

class TaskPage(val username: String, val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  def addTask(text: String) {
    fill("#add-task-text").`with`(text)
    find("#add-task").click()
  }

  def mustHaveTaskWithText(text: String) {
    await atMost(5, TimeUnit.SECONDS) until ".list-group-item span" hasText text
  }

  def mustNotHaveTaskWithText(text: String) {
    (await atMost(5, TimeUnit.SECONDS) until ".list-group-item span.done-true" withText text).isNotPresent
  }

  def toggleTaskWithText(text: String) {
    find(".list-group-item", withText(text)).find("input").click()

    await until ".list-group-item span.done-true" hasText text
  }

  def clearCompletedTasks = {
    find("#clear-completed").click()
  }

  def refresh: TaskPage = goTo(new TaskPage(username, driver, port))

  override def getUrl = s"http://localhost:$port/#/tasks"

  override def isAt() = await atMost(5, TimeUnit.SECONDS) until "#headline" containsText username
}

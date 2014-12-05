package ui.model

import java.util.concurrent.TimeUnit

import com.google.common.base.Predicate
import org.fluentlenium.core.FluentPage
import org.fluentlenium.core.filter.FilterConstructor._
import org.openqa.selenium.WebDriver

import scala.util.{Failure, Success, Try}

class TaskPage(val user: User, val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  def addTask(text: String) {
    fill("#add-task-text").`with`(text)
    find("#add-task").click()
  }

  def changePassword(from: String, to: String) = ???

  def mustHaveTaskWithText(text: String) {
    await atMost(5, TimeUnit.SECONDS) until ".list-group-item span" hasText text
  }

  def mustNotHaveTaskWithText(text: String) {
    await atMost(5, TimeUnit.SECONDS) until {
      Try {
        findFirst(".list-group-item span", withText(text))
      } match {
        case Success(_) => false
        case Failure(e) => true
      }
    }
  }

  implicit def fixPredicate[_, V](p: => Boolean): Predicate[V] = new Predicate[V] {
    def apply(p1: V) = p
  }

  def toggleTaskWithText(text: String) {
    find(".list-group-item", withText(text)).find("input").click()

    await until ".list-group-item span.done-true" hasText text
  }

  def clearCompletedTasks = {
    find("#clear-completed").click()
  }

  def refresh: TaskPage = goTo(new TaskPage(user, driver, port))

  override def getUrl = s"http://localhost:$port/#/tasks"

  override def isAt() = await atMost(5, TimeUnit.SECONDS) until "#headline" containsText user.firstName
}

package ui.pages

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

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
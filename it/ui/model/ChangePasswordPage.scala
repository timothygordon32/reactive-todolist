package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver
import security.User

class ChangePasswordPage(val user: User, val driver: WebDriver, val port: Int) extends FluentPage(driver) {

  def changePassword(from: String, to: String): TaskPage = {
    fill("#currentPassword").`with`(from)
    fill("#password1").`with`(to)
    fill("#password2").`with`(to)

    find("#changePassword").click()

    val tasksPage = new TaskPage(user, driver, port)
    await untilPage tasksPage isAt()
    tasksPage
  }

  override def getUrl = s"http://localhost:$port/#/password"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until "#currentPassword").isPresent
}
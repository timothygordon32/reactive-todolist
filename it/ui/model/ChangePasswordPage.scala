package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class ChangePasswordPage(val user: User, val driver: WebDriver, val port: Int) extends FluentPage(driver) {

  def changePasswordTo(newPassword: String): Unit = {
    fill("#currentPassword").`with`(user.password)
    fill("#password1").`with`(newPassword)
    fill("#password2").`with`(newPassword)

    find("#changePassword").click()
  }

  override def getUrl = s"http://localhost:$port/#/password"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until "#currentPassword").isPresent
}
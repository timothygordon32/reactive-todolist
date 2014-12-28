package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class ResetPasswordVerifiedPage(val uuid: String, val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  def enterPassword(password: String): Unit = enterPassword(password, password)

  def enterPassword(password1: String, password2: String) = {
    await untilPage this isAt()

    fill("#password1").`with`(password1)
    fill("#password2").`with`(password2)

    find("#reset").click()
  }

  override def getUrl = s"http://localhost:$port/#/reset/$uuid"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until ".form-auth-heading").isPresent
}
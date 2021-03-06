package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class ResetPasswordPage(val driver: WebDriver, val port: Int) extends FluentPage(driver) {

  def sendResetTokenToEmail(address: String): Unit = {
    await untilPage this isAt()

    fill("#email") `with` address

    find("#reset").click
  }

  override def getUrl = s"http://localhost:$port/#/reset"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until "#reset").isPresent
}
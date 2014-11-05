package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class SignUpVerifiedPage(val uuid: String, val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  override def getUrl = s"http://localhost:$port/#/signup/$uuid"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until ".form-auth-heading").isPresent
}
package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class SignUpVerifiedPage(val uuid: String, val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  def enterDetails(firstName: String, lastName: String, password1: String, password2: String) = {
    fill("#firstName").`with`(firstName)
    fill("#lastName").`with`(lastName)
    fill("#password1").`with`(password1)
    fill("#password2").`with`(password2)

    find("#sign-up").click()
  }

  override def getUrl = s"http://localhost:$port/#/signup/$uuid"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until ".form-auth-heading").isPresent
}
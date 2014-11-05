package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class SignUpPage(val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  def signUpWithEmail(address: String): Unit = {
    fill("#email") `with` address

    find("#signup").click
  }

   override def getUrl = s"http://localhost:$port/#/signup"

   override def isAt() = (await atMost(5, TimeUnit.SECONDS) until "#email").isPresent
 }
package ui.mail

import org.openqa.selenium.WebDriver
import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, Helpers, WebDriverFactory, WithBrowser}

abstract class WithMailServerAndBrowser(
    webDriver: WebDriver = WebDriverFactory(Helpers.HTMLUNIT),
    port: Int = Helpers.testServerPort,
    val mailServer: Wiser with SecurityMessages)
  extends WithBrowser(
    webDriver = webDriver, app = FakeApplication(
    additionalConfiguration = Map(
      "smtp.host" -> mailServer.getServer.getHostName,
      "smtp.port" -> mailServer.getServer.getPort)),
    port = port)

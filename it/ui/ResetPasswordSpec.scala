package ui

import play.api.test.{WebDriverFactory, PlaySpecification}
import ui.mail.{FakeMailServer, WithMailServerAndBrowser}
import ui.model.LoginPageSugar

class ResetPasswordSpec extends PlaySpecification {

  "Security" should {

    "allow user to reset their password" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", 10028).started,
      port = 19005) with LoginPageSugar {

      skipped

      val resetPassword = browser goTo loginPage resetPassword()
    }
  }
}

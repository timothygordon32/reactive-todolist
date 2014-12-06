package ui

import java.util.UUID

import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.{User, LoginPage}

class ChangePasswordSpec extends PlaySpecification {

  "Task page" should {

    "allow changing of password" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX), port = 19004) {
      skipped

      val loginPage = browser.goTo(new LoginPage(webDriver, port))

      val taskPage = loginPage.login(User.User1)

      val changePasswordPage = taskPage.changePassword

      val newPassword = UUID.randomUUID.toString
      changePasswordPage changePasswordTo newPassword

      browser.await untilPage taskPage isAt()
    }
  }
}

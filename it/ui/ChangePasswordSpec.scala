package ui

import java.util.UUID

import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.{User, LoginPage}

class ChangePasswordSpec extends PlaySpecification {

  "Task page" should {

    "allow changing of password" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX), port = 19004) {
      skipped

      val changePasswordPage = browser.goTo(new LoginPage(webDriver, port)).login(User.User1).changePassword

      val newPassword = UUID.randomUUID.toString
      changePasswordPage.changePassword(from = User.User1.password, to = newPassword)

      val restorePasswordPage = browser.goTo(new LoginPage(webDriver, port)).login(User.User1).changePassword
      restorePasswordPage.changePassword(from = newPassword, to = User.User1.password)
    }
  }
}

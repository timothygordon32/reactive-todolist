package ui

import play.api.test.{PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.{LoginPage, User}
import utils.UniqueStrings

class ChangePasswordSpec extends PlaySpecification with UniqueStrings {

  "Task page" should {

    "allow changing of password" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX), port = 19004) with LoginPageSugar {
      skipped

      val changePasswordPage = browser goTo loginPage login User.User1 changePassword()
      val newPassword = uniqueString

      changePasswordPage changePassword(from = User.User1.password, to = newPassword)

      val restorePasswordPage = browser goTo loginPage login(User.User1, newPassword) changePassword()
      restorePasswordPage changePassword(from = newPassword, to = User.User1.password)
    }
  }
}

trait LoginPageSugar {
  self: WithBrowser[Nothing] =>

  def loginPage = new LoginPage(webDriver, port)
}

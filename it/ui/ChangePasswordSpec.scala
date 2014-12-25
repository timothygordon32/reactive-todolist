package ui

import play.api.test._
import ui.mail._
import ui.model._
import utils.UniqueStrings

class ChangePasswordSpec extends PlaySpecification with UniqueStrings with SecurityMessageMatchers with Users {

  "Task page" should {

    "allow changing of password" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", 10027).started,
      port = 19004) with LoginPageSugar {

      val taskPage = browser goTo loginPage login User3
      val changePasswordPage = taskPage changePassword()
      val newPassword = uniqueString

      changePasswordPage changePassword(from = User3.password, to = newPassword)

      eventually(mailServer.messagesFor(User3.email) must have size 1)
      val message = (mailServer messagesFor User3.email).head
      message must bePasswordChanged

      val restorePasswordPage = taskPage changePassword()
      restorePasswordPage changePassword(from = newPassword, to = User3.password)
    }
  }
}


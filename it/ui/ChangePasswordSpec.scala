package ui

import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.User.User3
import ui.model.{LoginPage, SecurityMessageMatchers, SecurityMessages}
import utils.UniqueStrings

class ChangePasswordSpec extends PlaySpecification with UniqueStrings with SecurityMessageMatchers {

  "Task page" should {

    "allow changing of password" in new WithBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      app = FakeApplication(
        additionalConfiguration = Map(
          "smtp.host" -> "localhost",
          "smtp.port" -> 10027)
      ),
      port = 19004) with LoginPageSugar {

      val mailServer = new Wiser with SecurityMessages
      mailServer.setHostname("localhost")
      mailServer.setPort(10027)
      mailServer.start()

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

trait LoginPageSugar {
  self: WithBrowser[Nothing] =>

  def loginPage = new LoginPage(webDriver, port)
}

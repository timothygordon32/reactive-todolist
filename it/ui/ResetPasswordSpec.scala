package ui

import play.api.test.{PlaySpecification, WebDriverFactory}
import security.UserGeneration
import ui.mail.{FakeMailServer, WithMailServerAndBrowser}
import ui.model.{LoginPageSugar, ResetPasswordVerifiedPage}
import utils.UniqueStrings
import utils.PortAllocator._

class ResetPasswordSpec extends PlaySpecification with UserGeneration with UniqueStrings {

  "Security" should {

    "allow user to reset their password" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", mailPort).started,
      port = appPort) with LoginPageSugar {

      skipped

      val user = generateRegisteredUser
      val resetPassword = browser goTo loginPage resetPassword()

      resetPassword sendResetTokenToEmail user.email

      eventually(mailServer messagesFor user.email should have size 1)
      val message = (mailServer messagesFor user.email).head
      val verifiedPage = browser goTo new ResetPasswordVerifiedPage(message.passwordResetUuid, webDriver, port)
      val newPassword = uniqueString
      verifiedPage enterPassword newPassword
      eventually(mailServer messagesFor user.email should have size 2)
      browser goTo loginPage login(user, password = newPassword)
    }
  }
}

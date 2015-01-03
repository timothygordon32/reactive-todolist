package ui

import play.api.test.{PlaySpecification, WebDriverFactory}
import ui.mail.{SecurityMessageMatchers, FakeMailServer, WithMailServerAndBrowser}
import ui.model.LoginPageSugar
import utils.PortAllocator._
import utils.UniqueStrings

class UnknownEmailSpec extends PlaySpecification with UniqueStrings with SecurityMessageMatchers {

  "Security" should {

    "send out an email message to an unregistered email address advising that it is unknown" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", mailPort).started,
      port = appPort) with LoginPageSugar {

      val unknownEmail = s"unknown-user-$uniqueString@nomail.com"
      val resetPassword = browser goTo loginPage resetPassword()

      resetPassword sendResetTokenToEmail unknownEmail

      eventually(mailServer messagesFor unknownEmail should have size 1)
      val message = (mailServer messagesFor unknownEmail).head
      message must beUnknownEmailAddress
    }
  }
}

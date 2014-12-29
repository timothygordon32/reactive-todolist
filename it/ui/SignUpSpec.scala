package ui

import play.api.test.{PlaySpecification, WebDriverFactory}
import security.Users
import ui.mail.{FakeMailServer, WithMailServerAndBrowser}
import ui.model._
import utils.PortAllocator._

class SignUpSpec extends PlaySpecification with Users {

  "Sign-up process" should {

    "allow user sign-up" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", mailPort).started,
      port = appPort) with LoginPageSugar {

      val signUp = browser goTo loginPage signUp()

      var newUser = generateUnregisteredUser
      signUp signUpWithEmail newUser.email

      eventually(mailServer messagesFor newUser.email should have size 1)

      val message = (mailServer messagesFor newUser.email).head

      val signUpUuid = message.signUpUuid

      val verifiedPage = browser goTo new SignUpVerifiedPage(signUpUuid, webDriver, port)

      verifiedPage enterDetails(
        firstName = newUser.firstName, lastName = "Bloggs",
        password1 = newUser.password, password2 = newUser.password)

      eventually(mailServer messagesFor newUser.email should have size 2)

      browser goTo loginPage login newUser
    }
  }
}

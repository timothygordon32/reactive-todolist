package ui

import play.api.test._
import security.Users
import ui.mail._
import ui.model._
import utils.PortAllocator._

class AlreadySignedUpSpec extends PlaySpecification with SecurityMessageMatchers with Users {

  "Sign-up process" should {

    "email an existing user if they are already signed up" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", mailPort).started,
      port = appPort) with LoginPageSugar {

      val login = browser goTo loginPage

      val signUp = login signUp()
      browser.await untilPage signUp isAt()

      var alreadyRegisteredEmail = User1.email
      signUp signUpWithEmail alreadyRegisteredEmail

      eventually(mailServer.messagesFor(alreadyRegisteredEmail) must have size 1)

      val message = (mailServer messagesFor alreadyRegisteredEmail).head

      message must beAlreadySignedUp
    }
  }
}

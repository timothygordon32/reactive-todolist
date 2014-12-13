package ui

import play.api.test._
import ui.mail._
import ui.model._

class AlreadySignedUpSpec extends PlaySpecification with SecurityMessageMatchers {

  "Sign-up process" should {

    "email an existing user if they are already signed up" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMailServer("localhost", 10026).started,
      port = 19003) with LoginPageSugar {

      val login = browser goTo loginPage

      val signUp = login signUp()
      browser.await untilPage signUp isAt()

      var alreadyRegisteredEmail = User.User1.email
      signUp signUpWithEmail alreadyRegisteredEmail

      eventually(mailServer.messagesFor(alreadyRegisteredEmail) must have size 1)

      val message = (mailServer messagesFor alreadyRegisteredEmail).head

      message must beAlreadySignedUp
    }
  }
}

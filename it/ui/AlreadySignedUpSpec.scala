package ui

import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, PlaySpecification, WebDriverFactory, WithBrowser}
import ui.mail.{SecurityMessageMatchers, SecurityMessages}
import ui.model.{User, LoginPage}

class AlreadySignedUpSpec extends PlaySpecification with SecurityMessageMatchers {

  "Security" should {

    "email an existing user if they are already signed up" in new WithBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      app = FakeApplication(
        additionalConfiguration = Map(
          "smtp.host" -> "localhost",
          "smtp.port" -> 10026)
      ),
      port = 19003) {

      val mailServer = new Wiser with SecurityMessages
      mailServer.setHostname("localhost")
      mailServer.setPort(10026)
      mailServer.start()

      val login = browser.goTo(new LoginPage(webDriver, port))
      browser.await untilPage login isAt()

      val signUp = login.signUp
      browser.await untilPage signUp isAt()

      var alreadyRegisteredEmail = User.User1.email
      signUp signUpWithEmail alreadyRegisteredEmail

      eventually(mailServer.messagesFor(alreadyRegisteredEmail) must have size 1)

      val message = (mailServer messagesFor alreadyRegisteredEmail).head

      message must beAlreadySignedUp
    }
  }
}

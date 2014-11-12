package ui

import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.{SecurityMessageMatchers, SecurityMessages, LoginPage}

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

      val fakeMailServer = new Wiser with SecurityMessages
      fakeMailServer.setHostname("localhost")
      fakeMailServer.setPort(10026)
      fakeMailServer.start()

      val login = browser.goTo(new LoginPage(webDriver, port))
      browser.await untilPage login isAt()

      val signUp = login.signUp
      browser.await untilPage signUp isAt()

      var alreadyRegisteredEmail = "testuser1@nomail.com"
      signUp signUpWithEmail alreadyRegisteredEmail

      eventually(fakeMailServer.messagesFor(alreadyRegisteredEmail) must have size 1)

      val message = (fakeMailServer messagesFor alreadyRegisteredEmail).head

      message must beAlreadySignedUp
    }
  }
}

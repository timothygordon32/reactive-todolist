package ui

import org.specs2.time.Duration
import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.{LoginPage, EmailMessages}

class AlreadySignedUpSpec extends PlaySpecification {

  "Security" should {

    "email an existing user if they are already signed up" in new WithBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      app = FakeApplication(
        additionalConfiguration = Map(
          "smtp.host" -> "localhost",
          "smtp.port" -> 10026)
      ),
      port = 19003) {

      val fakeMailServer = new Wiser with EmailMessages
      fakeMailServer.setHostname("localhost")
      fakeMailServer.setPort(10026)
      fakeMailServer.start()

      val login = browser.goTo(new LoginPage(webDriver, port))
      browser.await untilPage login isAt()

      val signUp = login.signUp
      browser.await untilPage signUp isAt()

      var alreadyRegisteredEmail = "testuser1@nomail.com"
      signUp signUpWithEmail alreadyRegisteredEmail

      val oneSecond: Duration = new Duration(1000)

      eventually(fakeMailServer.messagesFor(alreadyRegisteredEmail) must have size 1)

      val message = (fakeMailServer messagesFor alreadyRegisteredEmail).head

      message.underlying.toString must contain ("already signed-up")
    }
  }
}

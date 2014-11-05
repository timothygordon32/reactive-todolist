package ui

import java.util.UUID

import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model.{EmailMessages, LoginPage, SignUpPage, SignUpVerifiedPage}

class SignUpSpec extends PlaySpecification {

  "Security" should {

    "allow user sign-up" in new WithBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      app = FakeApplication(
        additionalConfiguration = Map(
          "smtp.host" -> "localhost",
          "smtp.port" -> 10025)
      ),
      port = 19002) {

      val fakeMailServer = new Wiser with EmailMessages
      fakeMailServer.setHostname("localhost")
      fakeMailServer.setPort(10025)
      fakeMailServer.start()

      browser.await untilPage browser.goTo(new LoginPage(webDriver, port)) isAt()

      val signUp = new SignUpPage(webDriver, port)
      browser.await untilPage browser.goTo(signUp) isAt()

      var uniqueEmail = s"${UUID.randomUUID}@nomail.com"
      signUp signUpWithEmail uniqueEmail

      eventually {
        fakeMailServer messagesFor uniqueEmail should have size 1
      }

      val message = (fakeMailServer messagesFor uniqueEmail).head

      val signUpUuid = message signUpUuid()
      signUpUuid must not be None

      browser.await untilPage browser.goTo(new SignUpVerifiedPage(signUpUuid.get, webDriver, port)) isAt()
    }
  }
}

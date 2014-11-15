package ui

import java.util.UUID

import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, PlaySpecification, WebDriverFactory, WithBrowser}
import ui.model._

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

      val fakeMailServer = new Wiser with SecurityMessages
      fakeMailServer.setHostname("localhost")
      fakeMailServer.setPort(10025)
      fakeMailServer.start()

      val login = browser.goTo(new LoginPage(webDriver, port))
      browser.await untilPage login isAt()

      val signUp = login.signUp
      browser.await untilPage signUp isAt()

      var newUser = User.generate
      signUp signUpWithEmail newUser.userId

      eventually(fakeMailServer messagesFor newUser.userId should have size 1)

      val message = (fakeMailServer messagesFor newUser.userId).head

      val signUpUuid = message signUpUuid()
      signUpUuid must not be None

      val verifiedPage = browser.goTo(new SignUpVerifiedPage(signUpUuid.get, webDriver, port))
      browser.await untilPage verifiedPage isAt()

      verifiedPage enterDetails(
        firstName = newUser.firstName, lastName = "Bloggs",
        password1 = newUser.password, password2 = newUser.password)

      eventually(fakeMailServer messagesFor newUser.userId should have size 2)

      val loginPage = browser.goTo(new LoginPage(webDriver, port))
      val taskPage = loginPage.login(newUser)

      browser.await untilPage taskPage isAt()
    }
  }
}

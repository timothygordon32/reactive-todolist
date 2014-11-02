package ui

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.subethamail.wiser.Wiser
import play.api.test.{FakeApplication, WebDriverFactory, WithBrowser, PlaySpecification}

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

      val fakeMailServer = new Wiser
      fakeMailServer.setHostname("localhost")
      fakeMailServer.setPort(10025)
      fakeMailServer.start()

      val login = browser.goTo(s"http://localhost:$port/")
      (login.await atMost(5, TimeUnit.SECONDS) until "#username").isPresent

      val signUp = browser.goTo(s"http://localhost:$port/#/signup")
      (signUp.await atMost(5, TimeUnit.SECONDS) until "#email").isPresent

      var uniqueEmail = s"${UUID.randomUUID}@nomail.com"
      signUp.fill("#email") `with` uniqueEmail

      signUp.find("#signup").click

      eventually {
        fakeMailServer.getMessages should have size 1
      }

      val message = fakeMailServer.getMessages.get(0)
      message.getMimeMessage

      val regex = "/signup/([-a-f0-9]+)".r
      val link = regex.findFirstMatchIn(message.toString).map(_.group(1))

      link must not be None

      val signUpVerified = browser.goTo(s"http://localhost:$port/#/signup/${link.get}")
      (signUpVerified.await atMost(5, TimeUnit.SECONDS) until ".form-auth-heading").isPresent
    }
  }
}

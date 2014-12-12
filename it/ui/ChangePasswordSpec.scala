package ui

import org.openqa.selenium.WebDriver
import org.subethamail.wiser.Wiser
import play.api.test._
import ui.model.User.User3
import ui.model.{LoginPage, SecurityMessageMatchers, SecurityMessages}
import utils.UniqueStrings

abstract class WithMailServerAndBrowser(
    override val webDriver: WebDriver = WebDriverFactory(Helpers.HTMLUNIT),
    override val port: Int = Helpers.testServerPort,
    val mailServer: Wiser with SecurityMessages)
  extends WithBrowser(
    webDriver = webDriver, app = FakeApplication(
    additionalConfiguration = Map(
      "smtp.host" -> mailServer.getServer.getHostName,
      "smtp.port" -> mailServer.getServer.getPort)),
    port = port)

class FakeMakeServer(hostname: String, port: Int) extends Wiser with SecurityMessages {
  setHostname(hostname)
  setPort(port)

  def started = {
    start()
    this
  }
}

object FakeMakeServer {
  def apply(hostname: String, port: Int) = new FakeMakeServer(hostname, port)
}

class ChangePasswordSpec extends PlaySpecification with UniqueStrings with SecurityMessageMatchers {

  "Task page" should {

    "allow changing of password" in new WithMailServerAndBrowser(
      webDriver = WebDriverFactory(FIREFOX),
      mailServer = FakeMakeServer("localhost", 10027).started,
      port = 19004) with LoginPageSugar {

      val taskPage = browser goTo loginPage login User3
      val changePasswordPage = taskPage changePassword()
      val newPassword = uniqueString

      changePasswordPage changePassword(from = User3.password, to = newPassword)

      eventually(mailServer.messagesFor(User3.email) must have size 1)
      val message = (mailServer messagesFor User3.email).head
      message must bePasswordChanged

      val restorePasswordPage = taskPage changePassword()
      restorePasswordPage changePassword(from = newPassword, to = User3.password)
    }
  }
}

trait LoginPageSugar {
  self: WithBrowser[Nothing] =>

  def loginPage = new LoginPage(webDriver, port)
}

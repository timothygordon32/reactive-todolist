package ui.model

import java.util.concurrent.TimeUnit

import org.fluentlenium.core.FluentPage
import org.openqa.selenium.WebDriver

class LoginPage(val driver: WebDriver, val port: Int) extends FluentPage(driver) {
  def login(user: User): TaskPage = login(user, user.password)

  def login(user: User, password: String): TaskPage = {
    await untilPage this isAt()

    fill("#username").`with`(user.email)
    fill("#password").`with`(password)

    find("#login").click()

    val tasksPage = new TaskPage(user, driver, port)
    await untilPage tasksPage isAt()
    tasksPage
  }

  def signUp: SignUpPage = {
    find("#signup").click()

    val signUpPage = new SignUpPage(driver, port)
    await untilPage signUpPage isAt()
    signUpPage
  }

  override def getUrl = s"http://localhost:$port/"

  override def isAt() = (await atMost(5, TimeUnit.SECONDS) until "#username").isPresent
}
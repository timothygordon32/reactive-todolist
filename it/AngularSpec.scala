import java.util.UUID

import org.fluentlenium.core.Fluent
import play.api.libs.ws.WS
import play.api.libs.json.Json
import play.api.test.{WebDriverFactory, PlaySpecification, WithBrowser}

class AngularSpec extends PlaySpecification {

  "Application" should {

    "display a task" in new WithBrowser(webDriver = WebDriverFactory(FIREFOX)) {

      var label = "label-" + UUID.randomUUID

      await(WS.url("http://localhost:" + port + "/json/tasks").post(Json.obj("label" -> label)))

      val page = browser.goTo("http://localhost:" + port + "/app")

      page.await().until("ul li label span").hasText(label)
    }
  }

}

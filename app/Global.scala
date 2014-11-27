import java.lang.reflect.Constructor

import models.User
import play.api.Application
import play.api.libs.concurrent.Akka
import play.api.mvc.WithFilters
import securesocial.core.RuntimeEnvironment
import security.{MongoUserService, ApplicationRuntimeEnvironment}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends WithFilters(HttpsRedirectFilter) {
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(ApplicationRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}

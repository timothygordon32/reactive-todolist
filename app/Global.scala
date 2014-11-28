import java.lang.reflect.Constructor

import models.User
import play.api.Application
import play.api.libs.concurrent.Akka
import play.api.mvc.WithFilters
import play.modules.reactivemongo.ReactiveMongoPlugin
import securesocial.core.RuntimeEnvironment
import security.ApplicationRuntimeEnvironment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Global extends WithFilters(HttpsRedirectFilter) {
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance  = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(ApplicationRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

  override def onStart(app: Application): Unit = {
    ReactiveMongoPlugin.db(app)

    Akka.system(app).scheduler.scheduleOnce(60 seconds) {
      ApplicationRuntimeEnvironment.migrator.migrateProfileUsernameToEmail(_ => true)
    }

    Akka.system(app).scheduler.scheduleOnce(80 seconds) {
      ApplicationRuntimeEnvironment.migrator.migrateTaskOwnershipToUserObjectId(_ => true)
    }
  }
}

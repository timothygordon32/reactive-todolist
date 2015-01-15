import java.lang.reflect.Constructor

import controllers.{Authenticator, Home, Tasks}
import models.User
import play.api.mvc.WithFilters
import repository.MongoTaskRepository
import securesocial.core.RuntimeEnvironment
import security.{HttpsRedirectFilter, SecurityEnvironment}

import scala.reflect.ClassTag

object Global extends WithFilters(HttpsRedirectFilter) {

  lazy val taskRepository = MongoTaskRepository

  lazy val home = new Home
  lazy val authenticator = new Authenticator
  lazy val tasks = new Tasks(taskRepository)

  override def getControllerInstance[A](controllerClass: Class[A]): A = {

    def bind[T](value: T)(implicit ct: ClassTag[T]): Option[A] =
      if (controllerClass == ct.runtimeClass) Some(value.asInstanceOf[A]) else None

    val instance = bind(home) orElse
      bind(authenticator) orElse
      bind(tasks) orElse
      createSecureSocialController(controllerClass)

    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

  def createSecureSocialController[A](controllerClass: Class[A]): Option[A] =
    controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(SecurityEnvironment)
    }
}

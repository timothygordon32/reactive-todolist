import java.lang.reflect.Constructor

import controllers.{Home, Authenticator, Tasks}
import models.User
import play.api.mvc.WithFilters
import securesocial.core.RuntimeEnvironment
import security.{SecurityEnvironment, HttpsRedirectFilter, SecuredComponent}

object Global extends WithFilters(HttpsRedirectFilter) {

  val home: Home = new Home
  val authenticator: Authenticator = new Authenticator with SecuredComponent
  val tasks: Tasks = new Tasks with SecuredComponent

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    controllerClass match {
      case c if c == classOf[Home] => home
      case c if c == classOf[Authenticator] => authenticator
      case c if c == classOf[Tasks] => tasks
      case _ => createSecureSocialController(controllerClass).getOrElse(super.getControllerInstance(controllerClass))
    }
  }.asInstanceOf[A]

  def createSecureSocialController[A](controllerClass: Class[A]): Option[A] =
    controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(SecurityEnvironment)
    }
}

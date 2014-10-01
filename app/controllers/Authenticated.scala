package controllers

import models.User
import play.api.mvc.{Results, Security}
import play.api.mvc.Security.AuthenticatedBuilder

object Authenticated extends AuthenticatedBuilder[User] (
  request => request.session.get(Security.username).map(User),
  _ => Results.Unauthorized
)

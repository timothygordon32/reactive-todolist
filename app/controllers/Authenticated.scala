package controllers

import play.api.mvc.{Results, Security}
import play.api.mvc.Security.AuthenticatedBuilder

object Authenticated extends AuthenticatedBuilder (
  request => request.session.get(Security.username),
  _ => Results.Forbidden("Unlucky, sunshine")
)

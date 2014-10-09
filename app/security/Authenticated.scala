package security

import models.User
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.{Results, Security}

object Authenticated extends AuthenticatedBuilder[User] (
  request => request.session.get(Security.username).map(User),
  _ => Results.Unauthorized
)

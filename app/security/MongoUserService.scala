package security

import models.User
import play.api.Play
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import repository.{ProfileRepository, TokenRepository}
import securesocial.core.services.UserService
import Play.current

object MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {
  override def db: DB = ReactiveMongoPlugin.db
}

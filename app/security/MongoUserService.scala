package security

import models.User
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import repository.{ProfileRepository, TokenRepository}
import securesocial.core.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {
  override def db: DB = ReactiveMongoPlugin.db

  override def indexes(): Future[List[Index]] = {
    val profileIndexes = super[ProfileRepository].indexes()
    val tokenIndexes = super[TokenRepository].indexes()
    Future.sequence(List(profileIndexes, tokenIndexes)).map(_.flatten)
  }
}

package security

import models.User
import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import repository.{ProfileRepository, TokenRepository}
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.{PasswordInfo, AuthenticationMethod, BasicProfile}
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoUserService extends UserService[User] with ProfileRepository with TokenRepository {
  override def db: DB = ReactiveMongoPlugin.db

  override def indexes(): Future[List[Index]] = {
    val profileIndexes = super[ProfileRepository].indexes()
    val tokenIndexes = super[TokenRepository].indexes()
    Future.sequence(List(profileIndexes, tokenIndexes)).map(_.flatten)
  }

  private def seedTestUser = find(UsernamePasswordProvider.UsernamePassword, "testuser").flatMap { profile =>
    if (!profile.isDefined) save(BasicProfile(
      providerId = UsernamePasswordProvider.UsernamePassword,
      userId = "testuser",
      firstName = None,
      lastName = None,
      fullName = None,
      email = Some("testuser@nomail.com"),
      avatarUrl = None,
      authMethod = AuthenticationMethod.UserPassword,
      passwordInfo = Some(PasswordInfo(PasswordHasher.id, PasswordHash.hash("secret")))),
      SaveMode.SignUp)
    else Future.successful(profile)
  }

  if (!Play.isProd) seedTestUser
}

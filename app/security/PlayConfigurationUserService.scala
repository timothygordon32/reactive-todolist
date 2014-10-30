package security

import models.User
import play.api.{Logger, Play}
import play.api.Play.current
import securesocial.core.providers.MailToken
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.{SaveMode, UserService}
import securesocial.core.{AuthenticationMethod, BasicProfile, PasswordInfo}

import scala.concurrent.Future

trait PasswordInfoProvider {
  def passwordHashFor(username: String): Option[String]
}

object PlayConfigurationUserService extends PlayConfigurationUserService {
  val TestUserPasswordHash = PasswordHash.hash("secret1")

  override def passwordHashFor(username: String): Option[String] = {
    Play.current.configuration.getString(s"users.$username") match {
      case Some(hash) => Some(hash)
      case None => if (!Play.isProd && username == "testuser1") Some(TestUserPasswordHash) else None
    }
  }
}

trait PlayConfigurationUserService extends UserService[User] with PasswordInfoProvider {
  var tokens = List.empty[MailToken]
  var profiles = List.empty[BasicProfile]

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = Future.successful {
    Logger.info(s"Looking for user $userId")
    profiles.find(_.userId == userId) match {
      case Some(profile) => Some(profile)
      case None =>
        passwordHashFor(userId).map { hash =>
          val profile = BasicProfile(providerId, userId, None, None, None, Some(s"$userId@nomail.com"), None,
            authMethod = AuthenticationMethod.UserPassword,
            passwordInfo = Some(PasswordInfo(PasswordHasher.id, hash)))
          Logger.info(s"Found pre-loaded profile for user $userId")
          profile
        }
    }
  }

  override def passwordInfoFor(user: User): Future[Option[PasswordInfo]] = ???

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = Future.successful {
    None
  }

  override def deleteToken(uuid: String): Future[Option[MailToken]] = Future.successful {
    val found = tokens.find(_.uuid == uuid)
    tokens = tokens.filterNot(_.uuid == uuid)
    found
  }

  override def link(current: User, to: BasicProfile): Future[User] = ???

  override def save(profile: BasicProfile, mode: SaveMode): Future[User] = Future.successful {
    mode match {
      case SaveMode.LoggedIn => User(profile.userId)
      case SaveMode.SignUp =>
        profiles = profiles :+ profile
        User(profile.userId)
    }
  }

  override def findToken(token: String): Future[Option[MailToken]] = Future.successful(tokens.find(_.uuid == token))

  override def deleteExpiredTokens(): Unit = ???

  override def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = ???

  override def saveToken(token: MailToken): Future[MailToken] = Future.successful {
    tokens = token :: tokens
    token
  }
}

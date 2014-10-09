package security

import models.User
import securesocial.core.{BasicProfile, PasswordInfo}
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.Future

object PlayConfigurationUserService extends UserService[User] {
  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = ???

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = ???

  override def deleteToken(uuid: String): Future[Option[MailToken]] = ???

  override def link(current: User, to: BasicProfile): Future[User] = ???

  override def passwordInfoFor(user: User): Future[Option[PasswordInfo]] = ???

  override def save(profile: BasicProfile, mode: SaveMode): Future[User] = ???

  override def findToken(token: String): Future[Option[MailToken]] = ???

  override def deleteExpiredTokens(): Unit = ???

  override def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = ???

  override def saveToken(token: MailToken): Future[MailToken] = ???
}

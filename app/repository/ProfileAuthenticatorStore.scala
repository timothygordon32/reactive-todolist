package repository

import models.User
import securesocial.core.authenticator.{Authenticator, AuthenticatorStore}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

trait ProfileAuthenticatorStore[A <: Authenticator[User]] extends AuthenticatorStore[A] {

  def profiles: ProfileRepository

  def authenticatorDetails(authenticator: A): AuthenticatorDetails =
    AuthenticatorDetails(
      authenticator.id,
      authenticator.expirationDate,
      authenticator.lastUsed,
      authenticator.creationDate)

  def delete(id: String): Future[Unit] = profiles.deleteAuthenticator(id)

  def find(id: String)(implicit ct: ClassTag[A]): Future[Option[A]] =
    profiles.findAuthenticator(id).map(_.map(ua => toAuthenticator(ua.user, ua.authenticatorDetails)))

  def save(authenticator: A, timeoutInSeconds: Int): Future[A] =
    profiles.saveAuthenticator(authenticator.user, authenticatorDetails(authenticator)).map(_ => authenticator)

  def toAuthenticator(user: User, authenticator: AuthenticatorDetails): A
}

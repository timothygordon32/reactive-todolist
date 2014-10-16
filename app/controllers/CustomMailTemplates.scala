package controllers

import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import securesocial.controllers.MailTemplates
import securesocial.core.{IdentityProvider, BasicProfile}
import views.html.email.signUpHtml
import views.txt.email.signUpText

object CustomMailTemplates extends MailTemplates {
  override def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang) = {
    val link = s"${routes.Home.index().absoluteURL(IdentityProvider.sslEnabled)}#/signup/$token"
    (Some(signUpText(link)), Some(signUpHtml(link)))
  }

  override def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang) = ???

  override def getSendPasswordResetEmail(user: BasicProfile, token: String)(implicit request: RequestHeader, lang: Lang) = ???

  override def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = ???

  override def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = ???

  override def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = ???
}

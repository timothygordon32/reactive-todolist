package controllers

import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import securesocial.controllers.MailTemplates
import securesocial.core.{IdentityProvider, BasicProfile}
import views.html.email.{alreadyRegisteredEmailHtml, welcomeEmailHtml, signUpEmailHtml}
import views.txt.email.{alreadyRegisteredEmailText, welcomeEmailText, signUpEmailText}

object CustomMailTemplates extends MailTemplates {
  def baseUrl(implicit request: RequestHeader) = s"${routes.Home.index().absoluteURL(IdentityProvider.sslEnabled)}#"

  override def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang) = {
    val link = s"$baseUrl/signup/$token"
    (Some(signUpEmailText(link)), Some(signUpEmailHtml(link)))
  }

  override def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val signInLink = s"$baseUrl/login"
    val salutationText = user.firstName.fold("Welcome,")(name => s"Welcome $name,")
    val salutationHtml = user.firstName.fold(Html("Welcome"))(name => Html(s"Welcome $name,"))
    (Some(welcomeEmailText(salutationText, signInLink)), Some(welcomeEmailHtml(salutationHtml, signInLink)))
  }

  override def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang) = ???

  override def getSendPasswordResetEmail(user: BasicProfile, token: String)(implicit request: RequestHeader, lang: Lang) = ???

  override def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = ???

  override def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val salutationText = user.firstName.fold("Hey,")(name => s"Hey $name,")
    val salutationHtml = user.firstName.fold(Html("Hey"))(name => Html(s"Hey $name,"))
    (Some(alreadyRegisteredEmailText(salutationText)), Some(alreadyRegisteredEmailHtml(salutationHtml)))
  }
}

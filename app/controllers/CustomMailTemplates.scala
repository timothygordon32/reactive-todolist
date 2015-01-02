package controllers

import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import securesocial.controllers.MailTemplates
import securesocial.core.{BasicProfile, IdentityProvider}
import views.html.email._
import views.txt.email._

object CustomMailTemplates extends MailTemplates {
  def salutationText(salutation: String)(user: BasicProfile) = user.firstName.fold(s"$salutation,")(name => s"$salutation $name,")
  def salutationHtml(salutation: String)(user: BasicProfile) = user.firstName.fold(Html(s"$salutation,"))(name => Html(s"$salutation $name,"))

  val helloText = salutationText("Hey") _
  val helloHtml = salutationHtml("Hey") _

  val welcomeText = salutationText("Welcome") _
  val welcomeHtml = salutationHtml("Welcome") _

  def baseUrl(implicit request: RequestHeader) = s"${routes.Home.index().absoluteURL(IdentityProvider.sslEnabled)}#"

  def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang) = {
    val link = s"$baseUrl/signup/$token"
    (Some(signUpEmailText(link)), Some(signUpEmailHtml(link)))
  }

  def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val signInLink = s"$baseUrl/login"
    (Some(welcomeEmailText(welcomeText(user), signInLink)), Some(welcomeEmailHtml(welcomeHtml(user), signInLink)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang) = ???

  def getSendPasswordResetEmail(user: BasicProfile, token: String)
                                        (implicit request: RequestHeader, lang: Lang) = {
    val link = s"$baseUrl/reset/$token"
    (Some(passwordResetEmailText(helloText(user), link)), Some(passwordResetEmailHtml(helloHtml(user), link)))
  }

  def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    (Some(passwordChangedEmailText(helloText(user))), Some(passwordChangedEmailHtml(helloHtml(user))))
  }

  def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    (Some(alreadyRegisteredEmailText(helloText(user))), Some(alreadyRegisteredEmailHtml(helloHtml(user))))
  }
}

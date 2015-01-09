package controllers

import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import securesocial.controllers.MailTemplates
import securesocial.core.{BasicProfile, IdentityProvider}
import views.html.email._
import views.txt.email._

object CustomMailTemplates extends MailTemplates {
  def salutationText(salutation: String)(user: BasicProfile) =
    user.firstName.fold(s"$salutation,")(name => s"$salutation $name,")

  def salutationHtml(salutation: String)(user: BasicProfile) =
    user.firstName.fold(Html(s"$salutation,"))(name => Html(s"$salutation $name,"))

  val helloText = salutationText("Hey") _
  val helloHtml = salutationHtml("Hey") _

  val welcomeText = salutationText("Welcome") _
  val welcomeHtml = salutationHtml("Welcome") _

  def baseUrl(implicit request: RequestHeader) = s"${routes.Home.index().absoluteURL(IdentityProvider.sslEnabled)}#"

  def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang) = {
    val signUpTokenLink = s"$baseUrl/signup/$token"
    (Some(signUpEmailText(signUpTokenLink)),
      Some(signUpEmailHtml(signUpTokenLink)))
  }

  def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val loginLink = s"$baseUrl/login"
    (Some(welcomeEmailText(welcomeText(user), loginLink)),
      Some(welcomeEmailHtml(welcomeHtml(user), loginLink)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang) = {
    val signUpLink = s"$baseUrl/signup"
    (Some(unknownEmailText(signUpLink)),
      Some(unknownEmailHtml(signUpLink)))
  }

  def getSendPasswordResetEmail(user: BasicProfile, token: String)(implicit request: RequestHeader, lang: Lang) = {
    val resetTokenLink = s"$baseUrl/reset/$token"
    (Some(passwordResetEmailText(helloText(user), resetTokenLink)),
      Some(passwordResetEmailHtml(helloHtml(user), resetTokenLink)))
  }

  def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    (Some(passwordChangedEmailText(helloText(user))),
      Some(passwordChangedEmailHtml(helloHtml(user))))
  }

  def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val resetTokenLink = s"$baseUrl/reset"
    (Some(alreadyRegisteredEmailText(helloText(user), resetTokenLink)),
      Some(alreadyRegisteredEmailHtml(helloHtml(user), resetTokenLink)))
  }
}

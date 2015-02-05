package controllers

import java.util.UUID

import play.api.Play
import play.api.Play.current
import play.api.i18n.Lang
import play.api.mvc.{Result, Action, RequestHeader}
import play.twirl.api.{HtmlFormat, Html}
import securesocial.controllers.MailTemplates
import securesocial.core.{AuthenticationMethod, BasicProfile, IdentityProvider}
import views.html.email._
import views.txt.email._

object PreviewMailTemplates extends CustomMailTemplates {

  val NamedUser = BasicProfile("", "", Some("John"), None, None, None, None, AuthenticationMethod.UserPassword)
  val FakeToken = UUID.randomUUID().toString

  def signUpEmail = Action { implicit request =>
    preview(getSignUpEmail(FakeToken)._2.get _)
  }

  def alreadyRegisteredEmail = Action { implicit request =>
    preview(getAlreadyRegisteredEmail(NamedUser)._2.get _)
  }

  def welcomeEmail = Action { implicit request =>
    preview(getWelcomeEmail(NamedUser)._2.get _)
  }

  def unknownEmailNotice = Action { implicit request =>
    preview(getUnknownEmailNotice()._2.get _)
  }

  def sendPasswordResetEmail = Action { implicit request =>
    preview(getSendPasswordResetEmail(NamedUser, FakeToken)._2.get _)
  }

  def passwordChangedNoticeEmail = Action { implicit request =>
    preview(getPasswordChangedNoticeEmail(NamedUser)._2.get _)
  }

  private def preview(x: () => HtmlFormat.Appendable): Result = {
    if (Play.isDev)
      Ok(x())
    else NotFound
  }
}

object CustomMailTemplates extends CustomMailTemplates

trait CustomMailTemplates extends MailTemplates {

  def salutationText(salutation: String)(user: BasicProfile) =
    user.firstName.fold(s"$salutation,")(name => s"$salutation $name,")

  def salutationHtml(salutation: String)(user: BasicProfile) =
    user.firstName.fold(Html(s"$salutation,"))(name => Html(s"$salutation $name,"))

  val helloText = salutationText("Hey") _
  val helloHtml = salutationHtml("Hey") _

  val welcomeText = salutationText("Welcome") _
  val welcomeHtml = salutationHtml("Welcome") _

  def baseUrl(implicit request: RequestHeader) = s"${routes.Home.index().absoluteURL(IdentityProvider.sslEnabled)}#"
  def logoUrl(implicit request: RequestHeader) = routes.Assets.versioned("images/email.gif").absoluteURL(IdentityProvider.sslEnabled)

  def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang) = {
    val signUpTokenLink = s"$baseUrl/signup/$token"
    (Some(signUpEmailText(signUpTokenLink)),
      Some(signUpEmailHtml(signUpTokenLink, logoUrl)))
  }

  def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val resetLink = s"$baseUrl/reset"
    (Some(alreadyRegisteredEmailText(helloText(user), resetLink)),
      Some(alreadyRegisteredEmailHtml(helloHtml(user), resetLink, logoUrl)))
  }

  def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val loginLink = s"$baseUrl/login"
    (Some(welcomeEmailText(welcomeText(user), loginLink)),
      Some(welcomeEmailHtml(welcomeHtml(user), loginLink, logoUrl)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang) = {
    val signUpLink = s"$baseUrl/signup"
    (Some(unknownEmailText(signUpLink)),
      Some(unknownEmailHtml(signUpLink, logoUrl)))
  }

  def getSendPasswordResetEmail(user: BasicProfile, token: String)(implicit request: RequestHeader, lang: Lang) = {
    val resetTokenLink = s"$baseUrl/reset/$token"
    (Some(passwordResetEmailText(helloText(user), resetTokenLink)),
      Some(passwordResetEmailHtml(helloHtml(user), resetTokenLink, logoUrl)))
  }

  def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang) = {
    val resetLink = s"$baseUrl/reset"
    (Some(passwordChangedEmailText(helloText(user), resetLink)),
      Some(passwordChangedEmailHtml(helloHtml(user), resetLink, logoUrl)))
  }
}

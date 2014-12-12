package ui.mail

import org.specs2.matcher.Matcher
import org.subethamail.wiser.{Wiser, WiserMessage}
import play.api.test.PlaySpecification

import scala.collection.JavaConversions._

trait SecurityMessages {
  self: Wiser =>

  def messagesFor(address: String) = getMessages.filter(_.getEnvelopeReceiver.contains(address)).map(SecurityMessage.apply)
}

case class SecurityMessage(underlying: WiserMessage) {
  val SignUpRegEx = "/signup/([-a-f0-9]+)".r

  def signUpUuid() = SignUpRegEx.findFirstMatchIn(underlying.toString).map(_.group(1))
}

trait SecurityMessageMatchers {
  self: PlaySpecification =>

  val beAlreadySignedUp: Matcher[SecurityMessage] = (m: SecurityMessage) => (m.underlying.toString.contains("already signed-up"), "message should be already signed-up")

  val bePasswordChanged: Matcher[SecurityMessage] = (m: SecurityMessage) => (m.underlying.toString.contains("password has been changed"), "message should be password change notification")
}
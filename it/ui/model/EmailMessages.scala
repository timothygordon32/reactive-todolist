package ui.model

import org.subethamail.wiser.{Wiser, WiserMessage}

import scala.collection.JavaConversions._

trait EmailMessages {
  self: Wiser =>

  def messagesFor(address: String) = getMessages.filter(_.getEnvelopeReceiver.contains(address)).map(SecurityMessage)
}

case class SecurityMessage(underlying: WiserMessage) {
  val SignUpRegEx = "/signup/([-a-f0-9]+)".r

  def signUpUuid() = SignUpRegEx.findFirstMatchIn(underlying.toString).map(_.group(1))
}

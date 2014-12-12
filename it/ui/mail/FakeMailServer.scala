package ui.mail

import org.subethamail.wiser.Wiser
import ui.mail.SecurityMessages

class FakeMailServer(hostname: String, port: Int) extends Wiser with SecurityMessages {
  setHostname(hostname)
  setPort(port)

  def started = {
    start()
    this
  }
}

object FakeMailServer {
  def apply(hostname: String, port: Int) = new FakeMailServer(hostname, port)
}


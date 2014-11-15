package ui.model

import java.util.UUID

case class User(firstName: String, userId: String, password: String)

object User {
  val User1 = User(firstName = "Test1", userId = "testuser1@nomail.com", password = "secret1")

  def generate = {
    val uuid = UUID.randomUUID
    User(firstName = s"FirstName-$uuid", userId = s"test-userid-$uuid@nomail.com", password = s"test-password-$uuid")
  }
}
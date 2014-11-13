package ui.model

import java.util.UUID

case class User(firstName: String, userId: String, password: String)

object User {
  val User1 = User(firstName = "Test1", userId = "testuser1", password = "secret1")

  def generate = {
    val uuid = UUID.randomUUID
    User(s"FirstName-$uuid", s"test-userid-$uuid", s"test-password-$uuid")
  }
}
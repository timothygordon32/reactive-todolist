package security

case class User(firstName: String, userId: String, password: String) {
  def email = userId
}

trait Users extends UserGeneration {
  lazy val User1 = registerUser(User(firstName = "Test1", userId = "testuser1@nomail.com", password = "!secret1"))
  lazy val User2 = registerUser(User(firstName = "Test2", userId = "testuser2@nomail.com", password = "!secret2"))
  lazy val User3 = registerUser(User(firstName = "Test3", userId = "testuser3@nomail.com", password = "!secret3"))
}
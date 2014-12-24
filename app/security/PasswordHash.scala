package security

import org.mindrot.jbcrypt.BCrypt

object PasswordHash {
  def hash(password: String): HashedPassword = {
    val salt = BCrypt.gensalt(12)
    val hashedPassword = BCrypt.hashpw(password, salt)
    HashedPassword(hashedPassword, salt)
  }

  def verify(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}

case class HashedPassword(hashed: String, salt: String)

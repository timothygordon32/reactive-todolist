package controllers

import org.mindrot.jbcrypt.BCrypt

object PasswordHash {
  def hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

  def verify(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}

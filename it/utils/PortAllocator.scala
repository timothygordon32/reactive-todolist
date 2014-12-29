package utils

import java.net.{BindException, ServerSocket}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object PortAllocator {
  val defaultApplicationPort = 19000
  val defaultMailPort = 10025

  private val applicationPortAllocator = new PortAllocator(defaultApplicationPort)
  private val mailPortAllocator = new PortAllocator(defaultMailPort)

  def appPort = applicationPortAllocator.nextPort

  def mailPort = mailPortAllocator.nextPort
}

class PortAllocator(defaultPort: Int) {

  private var reservedPort = defaultPort
  private var guard = nextGuard

  def nextPort: Int = {
    val result = guard.getLocalPort
    release()
    guard = nextGuard
    result
  }

  @tailrec
  private def nextGuard: ServerSocket = {
    Try(new ServerSocket(reservedPort)) match {
      case Success(result) =>
        reservedPort = reservedPort + 1
        result
      case Failure(_:BindException) =>
        reservedPort = reservedPort + 1
        nextGuard
      case Failure(e) => throw e
    }
  }

  def release() = guard.close()
}

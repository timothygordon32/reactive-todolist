package utils

import java.net.{BindException, ServerSocket}

import play.api.test.PlaySpecification

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class PortAllocatorSpec extends PlaySpecification {

  "Port allocator" should {

    "reserve the default port of 19000" in {
      val portAllocator = new PortAllocator(19000)
      isReserved(19000) should beTrue

      val nextPort = portAllocator.nextPort

      portAllocator.release()
      nextPort should be equalTo 19000
    }

    "release the default port of 20000 just prior to use" in {
      val portAllocator = new PortAllocator(20000)
      portAllocator.nextPort should be equalTo 20000

      val defaultPortReserved = isReserved(20000)

      portAllocator.release()
      defaultPortReserved should beFalse
    }

    "allocate the next port 21001 after the default port 21000" in {
      val portAllocator = new PortAllocator(21000)
      portAllocator.nextPort should be equalTo 21000

      val nextPortAfterDefaultPort = isReserved(21001)

      portAllocator.release()
      nextPortAfterDefaultPort should beTrue
    }

    "allocate port 22002 if the next port after the default port 22000 is occupied" in {
      val portAllocator = new PortAllocator(22000)
      val reservedPort = new ServerSocket(22001)
      portAllocator.nextPort should be equalTo 22000
      reservedPort.close()

      val nextReportAfterOccupiedPortFree = isReserved(22002)

      portAllocator.release()
      nextReportAfterOccupiedPortFree should beTrue
    }
  }

  def isReserved(port: Int): Boolean = {
    Try {
      val socket = new ServerSocket(port)
      socket.close()
    }.isFailure
  }
}

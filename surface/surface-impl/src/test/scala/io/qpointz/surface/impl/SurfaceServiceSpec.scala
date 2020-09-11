package io.qpointz.surface.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.BeforeAndAfterAll
import io.qpointz.surface.api._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class SurfaceServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new SurfaceApplication(ctx) with LocalServiceLocator
  }

  val client: SurfaceService = server.serviceClient.implement[SurfaceService]

  override protected def afterAll(): Unit = server.stop()

  "surface service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}

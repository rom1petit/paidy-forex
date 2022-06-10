package forex.programs.emulator

import cats.effect.IO
import cats.effect.implicits.effectResourceOps
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import forex.config.OneFrameClientConfig
import forex.services.rates.interpreters.OneFrameLive
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, TestSuite}
import org.testcontainers.containers.wait.strategy.Wait

trait OneFrameServiceEmulator
    extends TestSuite
    with Matchers
    with BeforeAndAfterAll
    with ForAllTestContainer
    with Eventually
    with IntegrationPatience
    with ScalaFutures {

  override val container: GenericContainer = OneFrameServiceEmulator.oneFrameServiceContainer()

  def port: Int = container.mappedPort(8080)

  val configIO =
    IO(Uri.unsafeFromString(s"http://localhost:$port"))
      .map(endpoint => OneFrameClientConfig(endpoint, "10dc303535874aeccc86a8251e6992f5"))

  val serviceResource =
    for {
      config <- configIO.toResource
      client <- BlazeClientBuilder[IO].resource
    } yield {
      new OneFrameLive[IO](config, client)
    }
}

object OneFrameServiceEmulator {

  val ImageName = "paidyinc/one-frame:v0.3"

  def oneFrameServiceContainer(): GenericContainer =
    GenericContainer(
      dockerImage = ImageName,
      exposedPorts = Seq(8080),
      waitStrategy = Wait.defaultWaitStrategy()
    )
}

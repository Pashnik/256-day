import cats.data.Kleisli
import cats.effect.{Blocker, ContextShift, Effect, ExitCode, IO, IOApp}
import cats.syntax.functor._
import endpoints.EndpointsRouter
import org.http4s.syntax.kleisli._
import org.http4s.{Request, Response}
import fs2._
import org.http4s.server.blaze.BlazeServerBuilder
import org.log4s.getLogger

object Main extends IOApp {
  implicit val logger = getLogger(getClass)

  def httpApp[F[_]: Effect: ContextShift](
        blocker: Blocker
  ): Kleisli[F, Request[F], Response[F]] = EndpointsRouter.dayEndpoint[F].orNotFound

  def run(args: List[String]): IO[ExitCode] =
    (Stream
      .resource(Blocker[IO])
      .flatMap { blocker =>
        BlazeServerBuilder[IO]
          .bindHttp()
          .withHttpApp(httpApp[IO](blocker))
          .serve
      } >> Stream.eval(
        IO(
          logger.info(
            s"server started successfully on port: " +
            s"${org.http4s.server.defaults.HttpPort}"
        )
      )
    )).compile.drain
      .as(ExitCode.Success)
}

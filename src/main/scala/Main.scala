import cats.data.Kleisli
import cats.effect.{Blocker, ContextShift, Effect, ExitCode, IO, IOApp}
import cats.syntax.functor._
import endpoints.EndpointsRouter
import org.http4s.syntax.kleisli._
import org.http4s.{Request, Response}
import fs2._
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  def httpApp[F[_]: Effect: ContextShift](
        blocker: Blocker
  ): Kleisli[F, Request[F], Response[F]] = EndpointsRouter.dayEndpoint[F].orNotFound

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(Blocker[IO])
      .flatMap { blocker =>
        BlazeServerBuilder[IO]
          .bindHttp(port = 8080)
          .withHttpApp(httpApp[IO](blocker))
          .serve
      }
      .compile
      .drain
      .as(ExitCode.Success)
}

package endpoints

import cats.data.NonEmptyList
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, ParseFailure}
import io.circe.generic.auto._
import models.RequestModels._
import models.ResponseModels._
import org.log4s.Logger
import utils.TimeUtils._
import cats.syntax.flatMap._

object EndpointsRouter {
  def dayEndpoint[F[_]: Sync](implicit logger: Logger) = new DayEndpoint[F].routes
}

trait Endpoint[F[_]] {
  def routes(implicit logger: Logger): HttpRoutes[F]
}

class DayEndpoint[F[_]: Sync] extends Http4sDsl[F] with Endpoint[F] {

  object YearMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")
  object DateMatcher extends ValidatingQueryParamDecoderMatcher[CurrentDate]("currentDate")

  def routes(implicit logger: Logger): HttpRoutes[F] = {
    val collectFailures: NonEmptyList[ParseFailure] => String =
      _.toList.map(_.sanitized).mkString("\n")

    HttpRoutes.of[F] {
      case GET -> Root :? YearMatcher(year) =>
        year.fold(
            nelE => BadRequest(collectFailures(nelE))
          , year =>
            Sync[F].delay(logger.info(s"get following request: $year")) >>
              Ok(Response.ok(ComplicatedLogic.day256(year)))
        )
      case GET -> Root :? DateMatcher(date) =>
        date.fold(
            nelE => BadRequest(collectFailures(nelE))
          , date =>
            Sync[F].delay(logger.info(s"get following request: $date")) >>
              Ok(Response.ok(ComplicatedLogic.daysBetween(date)))
        )
    }
  }
}

object ComplicatedLogic {
  def day256(current: Year) = {
    val year = Year(current.value.toString.takeRight(2))
    DateResponse.of(
        day = if (isLeap(current)) 12 else 13
      , month = 9
      , year = year
    )
  }

  def daysBetween(date: CurrentDate) = {
    val current = inDays(date)
    DayResp.of(current match {
      case lessOrEq if lessOrEq <= progDay => progDay - lessOrEq
      case more if more > progDay          => progDay + rest(date.year)(more)
    })
  }
}

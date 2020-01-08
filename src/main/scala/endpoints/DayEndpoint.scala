package endpoints

import cats.data.NonEmptyList
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, ParseFailure}
import io.circe.generic.auto._
import models.RequestModels._
import models.ResponseModels._
import utils.TimeUtils._

object EndpointsRouter {
  def dayEndpoint[F[_]: Sync] = new DayEndpoint[F].routes
}

trait Endpoint[F[_]] {
  val routes: HttpRoutes[F]
}

class DayEndpoint[F[_]: Sync] extends Http4sDsl[F] with Endpoint[F] {

  object YearMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")
  object DateMatcher extends ValidatingQueryParamDecoderMatcher[CurrentDate]("currentDate")

  val routes: HttpRoutes[F] = {
    val collectFailures: NonEmptyList[ParseFailure] => String =
      _.toList.map(_.sanitized).mkString("\n")

    HttpRoutes.of[F] {
      case GET -> Root :? YearMatcher(year) =>
        year.fold(
            nelE => BadRequest(collectFailures(nelE))
          , year => Ok(Response.ok(ComplicatedLogic.day256(year)))
        )
      case GET -> Root :? DateMatcher(date) =>
        date.fold(
            nelE => BadRequest(collectFailures(nelE))
          , date => Ok(Response.ok(ComplicatedLogic.daysBetween(date)))
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
    DayResp.of(
        if (current < progDay) progDay - current
      else progDay + rest(date.year)(current)
    )
  }
}

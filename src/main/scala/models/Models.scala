package models

import cats.data.{NonEmptyList, Validated}
import models.RequestModels.Year
import cats.syntax.validated._
import org.http4s.{ParseFailure, QueryParamDecoder, QueryParameterValue}

object ResponseModels {
  case class Response(errorCode: Int, dataMessage: String)
  object Response {
    def ok(v: Valued): Response = Response(errorCode = 200, dataMessage = v.value)
  }

  sealed trait Valued { val value: String }
  case class DateResponse(value: String) extends Valued
  object DateResponse {
    def of(day: Int, month: Int, year: Year): DateResponse =
      DateResponse(s"$day/${if (month < 10) s"0$month" else month}/${year.value}")
  }
  case class DayResponse(value: String) extends Valued
  object DayResp {
    def of(day: Int): DayResponse = DayResponse(day.toString)
  }
}

object RequestModels {
  sealed trait Request           extends Product with Serializable
  case class Year(value: String) extends Request
  object Year {
    val length = 4
    implicit val tokenMatcherParamDecoder: QueryParamDecoder[Year] =
      (param: QueryParameterValue) =>
        if (param.value.length == this.length) Year(param.value).validNel
        else
          Validated.Invalid(NonEmptyList.one(ParseFailure("year should contain 4 numbers", s"${param.value}")))
  }
  case class CurrentDate(day: Int, month: Int, year: Year) extends Request
  object CurrentDate {
    private[this] def check(day: Int, month: Int): Boolean = {
      type Fn = Int => Boolean
      val checkDay: Fn   = day => day <= 31 && day >= 1
      val checkMonth: Fn = month => month >= 1 && month <= 12

      checkDay(day) && checkMonth(month)
    }

    implicit val userIdMatcherParamDecoder: QueryParamDecoder[CurrentDate] =
      (param: QueryParameterValue) => {
        val date                 = param.value
        val (day, (month, year)) = date.take(2) -> (date.substring(2, 4) -> date.takeRight(Year.length))
        Validated.condNel(
            check(day.toInt, month.toInt)
          , CurrentDate(day.toInt, month.toInt, Year(year))
          , ParseFailure("not valid month or day", s"$month,$day")
        )
      }
  }
}

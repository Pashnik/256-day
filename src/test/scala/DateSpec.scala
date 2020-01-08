import endpoints.ComplicatedLogic
import models.RequestModels.{CurrentDate, Year}
import models.ResponseModels.{DateResponse, DayResp}
import org.scalatest.{FlatSpec, Matchers}
import utils.TimeUtils.progDay

class DateSpec extends FlatSpec with Matchers {

  val leapYears    = List(Year("2000"), Year("2012"), Year("2020"))
  val nonLeapYears = List(Year("2013"), Year("2021"), Year("2200"))

  it should "right compute programmers day by year" in {
    val dayXLeap: Year => DateResponse    = year => DateResponse(s"12/09/${year.value.toString.takeRight(2)}")
    val dayXNonLeap: Year => DateResponse = year => DateResponse(s"13/09/${year.value.toString.takeRight(2)}")

    leapYears.map(ComplicatedLogic.day256) shouldBe leapYears.map(dayXLeap)
    nonLeapYears.map(ComplicatedLogic.day256) shouldBe nonLeapYears.map(dayXNonLeap)
  }

  it should "should right compute difference between current date and next x-day" in {
    val before = List(CurrentDate(21, 4, _), CurrentDate(17, 3, _), CurrentDate(1, 1, _))
    val after  = List(CurrentDate(21, 9, _), CurrentDate(14, 9, _), CurrentDate(31, 12, _))

    val beforeLeap    = leapYears.zip(before).map { case (x, f)    => f(x) }
    val beforeNonLeap = nonLeapYears.zip(before).map { case (x, f) => f(x) }

    val afterLeap    = leapYears.zip(after).map { case (x, f)    => f(x) }
    val afterNonLeap = nonLeapYears.zip(after).map { case (x, f) => f(x) }

    beforeLeap.map(ComplicatedLogic.daysBetween) shouldBe List(
        DayResp.of(progDay - 112)
      , DayResp.of(progDay - 77)
      , DayResp.of(progDay - 1)
    )

    beforeNonLeap.map(ComplicatedLogic.daysBetween) shouldBe List(
        DayResp.of(progDay - 111)
      , DayResp.of(progDay - 76)
      , DayResp.of(progDay - 1)
    )

    afterLeap.map(ComplicatedLogic.daysBetween) shouldBe List(
        DayResp.of(366 - 265 + progDay)
      , DayResp.of(366 - 258 + progDay)
      , DayResp.of(366 - 366 + progDay)
    )

    afterNonLeap.map(ComplicatedLogic.daysBetween) shouldBe List(
        DayResp.of(365 - 264 + progDay)
      , DayResp.of(365 - 257 + progDay)
      , DayResp.of(365 - 365 + progDay)
    )

    ComplicatedLogic.daysBetween(CurrentDate(11, 11, Year("2008"))) shouldBe DayResp.of(365 - 315 + 256)
  }
}

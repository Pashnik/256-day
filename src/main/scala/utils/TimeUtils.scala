package utils

import models.RequestModels.{CurrentDate, Year}

object TimeUtils {
  private[this] val months = List(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
  val leapYear             = 366
  val nonLeapYear          = 365

  val progDay = 256

  val rest: Year => Int => Int =
    current => day => if (isLeap(current)) leapYear - day else nonLeapYear - day

  def isLeap(year: Year): Boolean = {
    val value = year.value.toInt
    value % 4 == 0 && value % 100 != 0 || value % 400 == 0
  }

  def inDays(date: CurrentDate): Int =
    (if (isLeap(date.year)) months.updated(1, 29) else months).zipWithIndex
      .takeWhile { case (_, idx) => idx != date.month - 1 }
      .map(_._1)
      .sum +
      date.day
}

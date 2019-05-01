package models

import java.sql.Time

import doobie.implicits._

case class Teetime(time: Time, facility: Long) {

}

object Teetime extends DBOperator[Teetime] with Countable[Teetime] with Creatable.Simple[Teetime] {
  val field = fr"teetime"
  val valuenames = fr"time_, facility"
  def values(teetime: Teetime) = fr"${teetime.time},${teetime.facility}"
}
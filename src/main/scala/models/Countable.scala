package models

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._

trait Countable[A] {
  this : DBOperator[A] =>
  def count()(implicit xa: Aux[IO, Unit]): Option[Int] = {
    (sql"SELECT COUNT(*) FROM " ++ field).query[Int].option.transact(xa).unsafeRunSync
  }
}
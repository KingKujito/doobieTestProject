package models

import cats.effect.IO
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor.Aux

trait StandardGetters[A] extends Countable[A]{
  this : DBOperator[A] =>
  def getAll: Fragment = {
    sql"SELECT * FROM "++field
  }

  def getById(id : Long): Fragment = {
    sql"SELECT * FROM "++field++fr" WHERE id = $id"
  }

  /*
  def asOption(fr : Fragment)(implicit xa: Aux[IO, Unit]): Option[A] = {
    fr.query[A].option.transact(xa).unsafeRunSync
  }

  def asList(fr : Fragment)(implicit xa: Aux[IO, Unit]): List[A] = {
    fr.query[A].to[List].transact(xa).unsafeRunSync
  }*/
}

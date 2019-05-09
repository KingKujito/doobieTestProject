package models

import doobie.implicits._
import doobie.util.fragment.Fragment

trait StandardGetters[A] extends Countable[A]{
  this : DBOperator[A] =>
  def getAll(limit: Option[Int] = None): Fragment = {
    sql"SELECT * FROM "++field++{if(limit.isDefined)fr" LIMIT $limit" else fr""}
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

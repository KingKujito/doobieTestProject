package models

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

trait Creatable[A <: WithId]{
  this : DBOperator[A] =>

  def valuenames          : Fragment
  def values(a: A)        : Fragment

  def create(i: A)(implicit xa: Aux[IO, Unit]): Int = {
    (sql"INSERT INTO "++field++fr"("++valuenames++fr") VALUES ("++values(i)++fr")").update.run.transact(xa).unsafeRunSync
  }
}

object Creatable {
  trait Simple[A]{
    this : DBOperator[A] =>

    def valuenames          : Fragment
    def values(a: A)        : Fragment

    def create(i: A)(implicit xa: Aux[IO, Unit]): Int = {
      (sql"INSERT INTO "++field++fr"("++valuenames++fr") VALUES ("++values(i)++fr")").update.run.transact(xa).unsafeRunSync
    }
  }
}

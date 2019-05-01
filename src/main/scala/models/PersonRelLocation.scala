package models

import doobie.implicits._

object PersonRelLocation
  extends DBOperator[(Long, Long)] with Countable[(Long, Long)] with Creatable.Simple[(Long, Long)] {
  val field = fr"person_rel_location"
  val valuenames = fr"person, location"
  def values(personLocation: (Long, Long)) =
    fr"${personLocation._1},${personLocation._2}"

}


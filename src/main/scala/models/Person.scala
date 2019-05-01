package models

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

case class Person(
                   name: String,
                   override val id : Long = 0
                 ) extends WithId {

}

object Person extends DBOperator[Person] with StandardGetters[Person] with Creatable[Person] {
  val field = fr"person"
  val valuenames = fr"name"
  def values(person: Person) = fr"${person.name}"

  def getByName(name : String)(implicit xa: Aux[IO, Unit]): Option[Person] = {
    (sql"SELECT * FROM "++field++fr" WHERE name = $name").query[Person].option.transact(xa).unsafeRunSync
  }

  /**
    * Gets people within radius.
    * @param radius in kilometers
    */
  def getWithinRadius(lat: Float, long: Float, radius: Int, extension: Extension = defaultExtension)(implicit xa: Aux[IO, Unit])
  : List[Person] = {
    val distance = extension match {
      case PostGIS         => fr"ST_Distance(ST_Point($lat,$long)::geography, location.geog)"
      case Earthdistance   => fr"(point($lat, $long) <@> location.longlat)"}

    val within   = extension match {
      case PostGIS         => sql"ST_DWithin(ST_Point($lat,$long)::geography, location.geog, $radius*1000)"
      case Earthdistance   => sql"((point($lat, $long) <@> location.longlat) * 1.60934) < $radius"}

    (sql"SELECT * FROM "++field++fr""", person_rel_location, location
    WHERE person.id = person_rel_location.person AND location.id = person_rel_location.person AND
    """++within++fr"ORDER BY "++distance++fr" LIMIT 20")
      .query[Person].to[List].transact(xa).unsafeRunSync
  }
}
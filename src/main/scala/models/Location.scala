package models

import doobie.implicits._

case class Location (
                      lat:  Float,
                      long: Float,
                      override val id : Long = 0
                    ) extends WithId {
  lazy val geog = s"SRID=4326;POINT($lat $long)"
}

object Location extends DBOperator[Location] with Countable[Location] with Creatable[Location] {
  val field = fr"location"
  val valuenames = fr"longlat, geog"
  def values(location: Location) = fr"(point(${location.lat},${location.long})), ${location.geog}::geography"

}
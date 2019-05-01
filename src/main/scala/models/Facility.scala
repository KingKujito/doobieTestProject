package models

import doobie.implicits._

case class Facility(name: String, latitude: BigDecimal, longitude: BigDecimal,
                    override val id : Long = 0) extends WithId {
  //constructor
  def this(name: String, location: Location) = this(name, location.lat, location.long, 0)
}

object Facility extends DBOperator[Facility] with StandardGetters[Facility] with Creatable[Facility] {
  val field = fr"facility"
  val valuenames = fr"name, longitude, latitude"
  def values(facility: Facility) = fr"${facility.name},${facility.longitude},${facility.latitude}"
}
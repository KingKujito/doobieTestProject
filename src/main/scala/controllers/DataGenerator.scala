package controllers
import models._
import java.sql.Time

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import scala.util.Random

/**
  * This object fills the database.
  */
object DataGenerator {
  //The amount of rows we want our db to contain
  val desiredEntries = 500000

  /**
    * Put data for random people and random locations in the db, then randomly assign relations between them.
    */
  def populateDb()(implicit xa: Aux[IO, Unit]): Unit = {
    //logic for data creation
    val remaining = desiredEntries - Person.count.getOrElse(0)
    val locations = desiredEntries - Location.count.getOrElse(0)

    randomPeople(remaining)
      .foreach(Person.create)

    randomLocations(locations)
      .foreach(Location.create)

    //logic for assigning each location to people randomly
    val indx = Random.shuffle((1 until (desiredEntries+1)).toList)
    for(i <- 1 to desiredEntries) yield PersonRelLocation.create(
      i, indx(i-1))

    //checking if everything went as expected
    val p = Person.count.getOrElse(0)
    val l = Location.count.getOrElse(0)
    val r = PersonRelLocation.count.getOrElse(0)
    require(
      p == desiredEntries && p == l && p == r,
      s"The tables are out of sync... p=$p,l=$l,prl=$r...\nDeleting all tables and data...${utils.cleanupTable; ""} Please retry..."
    )

    //logic for data creation
    val facilities = desiredEntries - Facility.count.getOrElse(0)
    randomFacilities(facilities)
      .foreach(Facility.create)

    Facility.getAll.query[Facility].to[List].transact(xa).unsafeRunSync
      .foreach { f =>
        Teetime.create(Teetime(randomTime, f.id))
        Teetime.create(Teetime(randomTime, f.id))
        Teetime.create(Teetime(randomTime, f.id))
      }
  }

  def generateRandomFloat  : Float   = Random.nextFloat()

  /**
    * Scrape the web for random names and create people out of them
    */
  def randomPeople(amount: Int) : List[Person] = {
    val names = DataScraper.scrape(amount)
    (for (i <- 0 until amount) yield Person(names(i))).toList
  }

  /**
    * Scrape the web for random fake street names and create facilities out of them
    */
  def randomFacilities(amount: Int) : List[Facility] = {
    val names = DataScraper.scrape(amount, DataScraper.streetpath)
    (for (i <- 0 until amount) yield new Facility(names(i), randomLocation)).toList
  }

  def randomLocations(amount: Int) : List[Location] =
    (for (_ <- 0 until  amount) yield randomLocation).toList

  def randomLocation: Location = Location(generateRandomFloat*360-180, generateRandomFloat*180-90)

  def randomInt(min: Int, max: Int): Int = (generateRandomFloat*(max+min)-min).toInt

  //noinspection ScalaDeprecation
  def randomTime: Time = {
    new Time(
      randomInt(0,23),
      randomInt(0,59),
      randomInt(0,59))
  }
}

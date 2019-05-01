package controllers

import cats.effect.{ContextShift, IO}
import doobie._
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import models._
import utils._

import scala.concurrent.ExecutionContext

object Main {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  // A transactor that gets connections from java.sql.DriverManager and excutes blocking operations
  // on an unbounded pool of daemon threads. See the chapter on connection handling for more info.
  implicit val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql://localhost:5432/postgistest", // connect URL (driver-specific)
    "postgres",              // user
    "lunatech"                       // password
  )


  //Select which extension you'd like to use.
  val extension : Extension              = Earthdistance
  val generateData                       = false
  lazy val myCoordinates: List[Float]    = DataScraper.getMyLocation


  def main(args: Array[String]): Unit = {

    println(s"${Console.YELLOW}Note that a slow browser could cause this program to fail.${Console.RESET}")
    println(s"${Console.BLUE}START\n--------${Console.RESET}")

    if(generateData) {
      println(s"${Console.GREEN}Generating clean data...${Console.RESET}")
      //make sure we're working in a clean environment
      cleanupTable
      //setup our schema
      initTables
      //setup our data
      DataGenerator.populateDb()
      //check if everything went well
      require(
        Person              .count.contains(DataGenerator.desiredEntries) &&
        PersonRelLocation   .count.contains(DataGenerator.desiredEntries) &&
        Location            .count.contains(DataGenerator.desiredEntries),
        "Your actual data does not seem to comply with the desired data. Please check your db and/or the code"
      )
    }
    //then do this or whatever you want...

    println("People within 2000km of 0,0")
    Person.getWithinRadius(0,0,2000).foreach(println)

    println("\nPeople")
    println(Person.getById(11).query[Person].unique.transact(xa).unsafeRunSync)

    println("\nTeeTimes within radius")
    getTeetimesWithinRadius(0,0,2000).foreach(println)

    //benchmark()

    println(s"${Console.BLUE}--------\nEND${Console.RESET}")

  }


}

import cats.effect.IO
//import controllers.DataGenerator
import doobie.util.transactor.Transactor.Aux
import models._
import doobie._
import doobie.implicits._

package object utils {

  /**
    * Makes sure the DB connections are handled properly.
    */
  def withConnection(unit: Aux[IO, Unit] => Unit)(implicit xa: Aux[IO, Unit]): Unit = {
      unit.apply(xa)
  }

  /**
    * setup the schema
    */
  def initTables(implicit xa: Aux[IO, Unit]): Int = {
    sql"""
     CREATE SEQUENCE IF NOT EXISTS location_id_seq;
     CREATE SEQUENCE IF NOT EXISTS person_id_seq;
     CREATE SEQUENCE IF NOT EXISTS facility_id_seq;


     CREATE TABLE IF NOT EXISTS facility (
              name character varying(50) COLLATE pg_catalog."default" NOT NULL,
              longitude decimal,
              latitude decimal,
              id bigint NOT NULL DEFAULT nextval('facility_id_seq'::regclass),
              CONSTRAINT facility_pkey PRIMARY KEY (id)
          )
          WITH ( OIDS = FALSE )
          TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS teetime (
              time_ time NOT NULL,
              facility bigint NOT NULL,
              CONSTRAINT facility FOREIGN KEY (facility)
                  REFERENCES public.facility (id) MATCH SIMPLE
                  ON UPDATE NO ACTION
                  ON DELETE CASCADE
          )
          WITH ( OIDS = FALSE )
          TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS location (
         longlat point,
         geog geography,
         id bigint NOT NULL DEFAULT nextval('location_id_seq'::regclass),
         CONSTRAINT location_pkey PRIMARY KEY (id)
     )
     WITH ( OIDS = FALSE )
     TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS person (
         name character varying(50) COLLATE pg_catalog."default" NOT NULL,
         id bigint NOT NULL DEFAULT nextval('person_id_seq'::regclass),
         CONSTRAINT person_pkey PRIMARY KEY (id)
     )
     WITH ( OIDS = FALSE )
     TABLESPACE pg_default;


     CREATE TABLE IF NOT EXISTS person_rel_location (
         person bigint NOT NULL,
         location bigint NOT NULL,
         CONSTRAINT location FOREIGN KEY (location)
             REFERENCES public.location (id) MATCH SIMPLE
             ON UPDATE NO ACTION
             ON DELETE CASCADE,
         CONSTRAINT person FOREIGN KEY (person)
             REFERENCES public.person (id) MATCH SIMPLE
             ON UPDATE NO ACTION
             ON DELETE CASCADE
     )
     WITH ( OIDS = FALSE )
     TABLESPACE pg_default;
    """.update.run.transact(xa).unsafeRunSync
  }

  /**
    * Remove all data and clean up the schema (also resets sequences).
    */
  def cleanupTable(implicit xa: Aux[IO, Unit]): Int ={
    sql"""
     DROP SEQUENCE IF EXISTS location_id_seq CASCADE;
     DROP SEQUENCE IF EXISTS person_id_seq CASCADE;
     DROP SEQUENCE IF EXISTS facility_id_seq CASCADE;

     DROP TABLE IF EXISTS person_rel_location;
     DROP TABLE IF EXISTS location;
     DROP TABLE IF EXISTS person;
     DROP TABLE IF EXISTS teetime;
     DROP TABLE IF EXISTS facility;
    """.update.run.transact(xa).unsafeRunSync
  }

  /**
    * Gets people, locations and distances within radius from (equator, meridian).
    * @param radius in kilometers
    */
 /* def getNearMiddle(radius: Int, limit: Int = 20, extension: Extension = defaultExtension)(implicit xa: Aux[IO, Unit])
  : List[(Person, Location, Float)] = {
    (sql"""
         SELECT
         	person.name AS name,
          person.id AS personid,
         	location.longlat AS position,
          location.id AS locationid,
          """++distanceQuery(0,0, extension=extension)++fr""" AS dist_in_km
         	FROM person, person_rel_location, location
             WHERE person.id = person_rel_location.person AND location.id = person_rel_location.person AND
             """++withinQuery(0,0, radius, extension=extension)++fr"""
             ORDER BY dist_in_km
             LIMIT $limit
    """).query[(Person, Location, Float)].to[List].transact(xa).unsafeRunSync
  }*/

  def distanceQuery(lat: Float, long: Float,
                    table: Fragment = fr"location", extension: Extension = defaultExtension): Fragment = extension match {
    case PostGIS         => fr"ST_Distance(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, "++table++fr".geog)/1000"
    case Earthdistance   => fr"((point($lat, $long) <@> "++table++fr".longlat)*1.60934)"}

  def withinQuery(lat: Float, long: Float, radius: Int,
                  table: Fragment = fr"location", extension: Extension = defaultExtension): Fragment = extension match {
    case PostGIS         => fr"ST_DWithin(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, "++table++fr".geog, $radius*1000)"
    case Earthdistance   => fr"((point($lat, $long) <@> "++table++fr".longlat)*1.60934) < $radius"}


  case class FacilTeeFloat(facility: Facility, teetime: Teetime, float: Float)
  /**
    * Gets facilities, teetimes and distances within radius from (long, lat).
    * @param radius in kilometers
    */
 def getTeetimesWithinRadius(lat: Float, long: Float, radius: Int, limit: Int = 20, extension: Extension = defaultExtension)(implicit xa: Aux[IO, Unit])
  : List[FacilTeeFloat] = {
    (sql"SELECT *, "++distanceQuery2(lat, long,  extension=extension)++fr""" AS dist_in_km
    FROM facility, teetime
    WHERE """++withinQuery2(lat, long, radius, extension=extension)++fr""" AND teetime.facility = facility.id
    ORDER BY dist_in_km LIMIT $limit""").query[FacilTeeFloat].to[List].transact(xa).unsafeRunSync
  }

  def distanceQuery2(lat: Float, long: Float,
                    table: Fragment = fr"facility", extension: Extension = defaultExtension): Fragment = extension match {
    case PostGIS         =>
      fr"ST_Distance(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, ('SRID=4326;POINT('||"++table++fr".latitude||' '||"++table++fr".longitude||')')::geography)/1000"
    case Earthdistance   => fr"((point($lat,$long) <@> point("++table++fr".latitude, "++table++fr".longitude))*1.60934)"}

  def withinQuery2(lat: Float, long: Float, radius: Int,
                  table: Fragment = fr"facility", extension: Extension = defaultExtension): Fragment = extension match {
    case PostGIS         =>
      fr"ST_DWithin(('SRID=4326;POINT('||$lat||' '||$long||')')::geography, ('SRID=4326;POINT('||"++table++fr".latitude||' '||"++table++fr".longitude||')')::geography, $radius*1000)"
    case Earthdistance   => fr"((point($lat,$long) <@> point("++table++fr".latitude, "++table++fr".longitude))*1.60934) < $radius"}



  /**
    * Gets facilities, teetimes and distances within radius from (long, lat).
    * @param radius in kilometers
    */
  def benchmarker(lat: Float, long: Float, radius: Int, extension: Extension = defaultExtension)(implicit xa: Aux[IO, Unit])
  : String = {
    (sql"""
         EXPLAIN ANALYZE
    SELECT """ ++ distanceQuery2(lat, long,  extension=extension) ++ fr"""AS dist_in_km
    FROM facility
    WHERE """ ++ withinQuery2(lat, long, radius, extension=extension) ++ fr"""
    ORDER BY dist_in_km""").query[String].to[List].transact(xa).unsafeRunSync.apply(11) //apply(11) for large data
  }

  def benchmark (benchTries : Int = 25)(implicit xa: Aux[IO, Unit]): Unit = {
    val benchE = for(_ <- 1 to benchTries) yield benchmarker(0,0,30000, extension = Earthdistance)
    val benchP = for(_ <- 1 to benchTries) yield benchmarker(0,0,30000, extension = PostGIS)

//    println(s"${Console.BLUE}For ${DataGenerator.desiredEntries} rows\n------")
    println(s"${Console.GREEN}Earthdistance performance${Console.RESET}")
    benchE.foreach(println)

    println(s"${Console.GREEN}Average: ${average(
      benchE.map(_.replaceAll("[a-zA-z: ]", "").toFloat).toList
    )}${Console.RESET} ms\n")

    println(s"${Console.GREEN}PostGIS performance${Console.RESET}")
    benchP.foreach(println)

    println(s"${Console.GREEN}Average: ${average(
      benchP.map(_.replaceAll("[a-zA-z: ]", "").toFloat).toList
    )}${Console.RESET} ms\n")
  }

  def average (list : List[Float]): Float = list.sum / list.length
}

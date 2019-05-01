# PostGIS test project

This project has been created to test out and play around with PostGIS.

As of right now this application creates a schema and populates the tables. After this you can play around with PostGis and earthdistance.

### Conventions
- name your column 'longlat' when storing coordinates as a Postgres point type
- name your column 'geog' when storing coordinates as a PostGIS geography type

### Requirements
- sbt installed
- Safari browser (with allow remote automation enabled)
- a Postgres db on port 5432 called 'postgistest'(or edit 'xa's config in Main.scala)
- define setup a username and password in controllers.Main.xa
- the cube, earthdistance and postgis extensions setup in your db

### Instructions
- make sure you meet all requirements mentioned above
- alter the code to test whatever you want
- set Main.generateData to false if you don't want your data to be replaced
- sbt run

congrats! Your schema should now look as follows:

### Sequences
- location_id_seq
- person_id_seq
- facility_id_seq

### Tables
 - location(id, point, geog)
 - person(id, name)
 - person_rel_location(person, location)
 - facility(name, longtitude, latitude)
 - teetime(time_, facility)
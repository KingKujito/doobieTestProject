--Uncomment any of these queries to play around with either PostGIS or earthdistance.
/*SELECT ST_Distance(
  'SRID=4326;POINT(0 0)'::geography,
  'SRID=4326;POINT(20 20)'::geography
  )/1000 as dist_in_km;*/
--results in roughly 3106

/*SELECT ST_Distance(
  'SRID=4326;POINT(0 0)'::geography,
  'SRID=4326;POINT(20 20)'::geography,
  false
  )/1000 as dist_in_km;*/
--results in 3112.44932520722

/*SELECT ST_DWithin(
  'SRID=4326;POINT(0 0)'::geography,
  'SRID=4326;POINT(20 20)'::geography,
   3112*1000);*/
--result in true

--SELECT (point(0, 0) <@> point(20, 20)) * 1.60934 as dist_in_km;
--Result: 3112.4308966856 km
--https://gps-coordinates.org/distance-between-coordinates.php Result: 3112.45 km
--manual inspection on Google Maps Result: 3112.45 km


--Quick utility queries
--DELETE FROM person;
--SELECT * FROM person
--DELETE FROM location;
--SELECT * FROM location
--SELECT * FROM person_rel_location

--The following two queries both return a list of people within 5000km of point(0,0) on earth, ordered on
--their distance to this point. This has been confirmed to be accurate.
/*
SELECT 
	person.name AS name,
	location.longlat AS position,
    ((point(0, 0) <@> location.longlat)*1.60934) AS dist_in_km
	FROM person, person_rel_location, location
    WHERE person.id = person_rel_location.person AND location.id = person_rel_location.person AND
    ((point(0, 0) <@> location.longlat)*1.60934) < 5000
    ORDER BY dist_in_km
    LIMIT 20;
	*/

/*
SELECT 
	person.name AS name,
	location.longlat AS position,
    ST_Distance('SRID=4326;POINT(0 0)'::geography, location.geog)/1000 AS dist_in_km
	FROM person, person_rel_location, location
    WHERE person.id = person_rel_location.person AND location.id = person_rel_location.person AND
    ST_DWithin('SRID=4326;POINT(0 0)'::geography, location.geog, 5000*1000)
    ORDER BY dist_in_km;
	*/
	
--SELECT * FROM facility;

/*
SELECT *, ST_Distance('SRID=4326;POINT(0 0)'::geography,
					  ('SRID=4326;POINT('||facility.longitude||' '||facility.latitude||')')::geography)/1000 AS dist_in_km
	FROM facility
    WHERE ST_DWithin('SRID=4326;POINT(0 0)'::geography,
					  ('SRID=4326;POINT('||facility.longitude||' '||facility.latitude||')')::geography,
					 5000*1000)
    ORDER BY dist_in_km;*/
	--works
	
/*
SELECT *, ((point(0,0) <@> point(facility.longitude,facility.latitude))*1.60934) AS dist_in_km
	FROM facility, teetime
    WHERE ((point(0,0) <@> point(facility.longitude,facility.latitude))*1.60934) < 5000 AND teetime.facility = facility.id
    ORDER BY dist_in_km;*/
	--works
	
/*	SELECT *, ((point(0,0) <@> point(facility.longitude,facility.latitude))*1.60934) AS dist_in_km
	FROM facility, teetime
    WHERE teetime.facility = facility.id
    ORDER BY dist_in_km;*/
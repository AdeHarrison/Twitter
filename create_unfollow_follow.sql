CREATE OR REPLACE
FUNCTION create_unfollow ()
RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN
DROP TABLE IF EXISTS following_and_not_follow_me CASCADE;

-- people I follow (friends) that are not followers
CREATE TABLE following_and_not_follow_me AS
SELECT
	fr.id,
	fr.twitter_id,
	fr.name,
	fr.screen_name,
	fr.description, 
	fr.location,
	fr.followers_count,
	fr.friends_count,
	fr.protected
FROM
	friend fr
WHERE
	fr.twitter_id NOT IN (
	SELECT
		fo.twitter_id
	FROM
		follower fo);

DELETE
FROM
	unfollow;

-- people I follow (friends) that are not followers and not fixed who arn't expected to follow me 
INSERT
	INTO
	unfollow (twitter_id, id, name,	screen_name, description, location, followers_count, friends_count, protected)
SELECT
	fanfm.twitter_id,
	fanfm.id,
	fanfm.name,
	fanfm.screen_name,
	fanfm.description, 
	fanfm.location,
	fanfm.followers_count,
	fanfm.friends_count,
	fanfm.protected 
FROM
	following_and_not_follow_me fanfm
WHERE
	fanfm.twitter_id NOT IN (
	SELECT
		f.twitter_id
	FROM
		fixed f);

SELECT
	count(*)
INTO
	total
FROM
	unfollow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;


CREATE OR REPLACE
FUNCTION create_follow ()
RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN
DROP TABLE IF EXISTS follow_me_and_not_following CASCADE;

CREATE TABLE follow_me_and_not_following AS
SELECT
	fo.twitter_id,
	fo.id,
	fo.name,
	fo.screen_name,
	fo.description, 
	fo.location,
	fo.followers_count,
	fo.friends_count,
	fo.protected 
FROM
	follower fo
WHERE
	fo.twitter_id NOT IN (
	SELECT
		fr.twitter_id
	FROM
		friend fr);

SELECT
	count(*)
INTO
	total
FROM
	follow_me_and_not_following;

DELETE
FROM
	follow;

INSERT
	INTO follow (twitter_id, id, name,	screen_name, description, location, followers_count, friends_count, protected)
SELECT
	fmanf.twitter_id,
	fmanf.id,
	fmanf.name,
	fmanf.screen_name,
	fmanf.description, 
	fmanf.location,
	fmanf.followers_count,
	fmanf.friends_count,
	fmanf.protected
FROM
	follow_me_and_not_following fmanf
WHERE
	(fmanf.followers_count >2500 AND fmanf.friends_count >2500)
	OR lower(fmanf.description) LIKE '%brexit%'
	OR lower(fmanf.description) LIKE '%english%'
	OR lower(fmanf.description) LIKE '%brit%'
	OR lower(fmanf.description) LIKE '%army%'
	OR lower(fmanf.description) LIKE '%navy%'
	OR lower(fmanf.description) LIKE '%raf%'
	OR lower(fmanf.description) LIKE '%para%'
	OR lower(fmanf.description) LIKE '%marine%'
	OR lower(fmanf.description) LIKE '%woke%'
	OR lower(fmanf.description) LIKE '%patriot%'
	OR lower(fmanf.description) LIKE '%military%'
	OR lower(fmanf.description) LIKE '%farage%'
	OR lower(fmanf.description) LIKE '%trump%'
	OR lower(fmanf.description) LIKE '%cenotaph%'
	OR lower(fmanf.description) LIKE '%royalist%'
	OR lower(fmanf.description) LIKE '%gb news%'
	OR lower(fmanf.description) LIKE '%gbnew%'
	OR lower(fmanf.description) LIKE '%blighty%'
	OR lower(fmanf.description) LIKE '%queen%'
	OR lower(fmanf.description) LIKE '%uk%'
	OR lower(fmanf.description) LIKE '%woke%'
	OR lower(fmanf.description) LIKE '%🇬🇧%';

SELECT
	count(*)
INTO
	total
FROM
	follow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;
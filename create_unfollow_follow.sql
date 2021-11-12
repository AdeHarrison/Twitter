CREATE OR REPLACE
FUNCTION create_unfollow ()
RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN
DROP TABLE IF EXISTS following_and_not_follow_me CASCADE;

CREATE TABLE following_and_not_follow_me AS
SELECT
	fr.twitter_id,
	fr.name,
	fr.screen_name
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

INSERT
	INTO
	unfollow
SELECT
	fanfm.twitter_id,
	fanfm.name,
	fanfm.screen_name
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
	fo.name,
	fo.screen_name,
	fo.description
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
	INTO
	follow
SELECT
	*
FROM
	follow_me_and_not_following fmanf
WHERE
	lower(fmanf.description) LIKE '%brexit%'
	OR lower(fmanf.description) LIKE '%english%'
	OR lower(fmanf.description) LIKE '%british%'
	OR lower(fmanf.description) LIKE '%britain%'
	OR lower(fmanf.description) LIKE '%army%'
	OR lower(fmanf.description) LIKE '%para%'
	OR lower(fmanf.description) LIKE '%woke%';

SELECT
	count(*)
INTO
	total
FROM
	follow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;
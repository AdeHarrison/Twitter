CREATE OR REPLACE FUNCTION create_unfollow ()
RETURNS integer AS $total$
declare
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

delete from unfollow;

insert into unfollow
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

   SELECT count(*) into total FROM unfollow;
   RETURN total;
END;
$total$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_follow ()
RETURNS integer AS $total$
declare
	total integer;
BEGIN

DROP TABLE IF EXISTS follow_me_and_not_following CASCADE;

CREATE TABLE follow_me_and_not_following AS
SELECT
	fo.twitter_id,
	fo.name,
	fo.screen_name
FROM
	follower fo
WHERE
	fo.twitter_id NOT IN (
	SELECT
		fr.twitter_id
	FROM
		friend fr);

    RETURN 789;
--delete from unfollow;
--
--insert into unfollow
--SELECT
--	fanfm.twitter_id,
--	fanfm.name,
--	fanfm.screen_name
--FROM
--	following_and_not_follow_me fanfm
--WHERE
--	fanfm.twitter_id NOT IN (
--	SELECT
--		f.twitter_id
--	FROM
--		fixed f);
--
--   SELECT count(*) into total FROM unfollow;
--   RETURN total;
END;
$total$ LANGUAGE plpgsql;
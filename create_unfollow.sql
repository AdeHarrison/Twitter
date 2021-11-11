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
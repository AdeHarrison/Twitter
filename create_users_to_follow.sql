CREATE OR REPLACE
FUNCTION create_users_to_follow ()
    RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN

DELETE
FROM
	follow;

-- 1. Select ALL users that follow me and i don't follow
INSERT
	INTO
	follow
    SELECT
	*
FROM
	follower fo
WHERE
	fo.twitter_id NOT IN (
	SELECT
		fr.twitter_id
	FROM
		friend fr);

-- 2. Don't follow users with protected tweets
DELETE
--SELECT * 
FROM
	follow f
WHERE
	f.protected = TRUE;

-- 3. Don't follow users that I've followed before and still waiting for follow back
DELETE
--SELECT * 
FROM
	follow f
WHERE
	f.twitter_id IN
(
		SELECT
			fp.twitter_id
		FROM
			follow_pending fp
	);

-- 4. Don't follow users that I've followed before (and been unfollowed) or outside follow back period
DELETE
--SELECT * 
FROM
	follow f
WHERE
	f.twitter_id IN
(
		SELECT
			fi.twitter_id
		FROM
			follow_ignore fi
	);
	
-- 3. Return number of users to follow
SELECT
	COUNT(*)
    INTO
	total
FROM
	follow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;
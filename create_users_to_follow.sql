CREATE OR REPLACE
FUNCTION create_users_to_follow ()
    RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN

DELETE
FROM
	to_follow;

-- 1. Select ALL users that follow me and i don't follow i.e followers not yet friends
INSERT
	INTO
	to_follow
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
	to_follow tf
WHERE
	tf.protected = TRUE;

-- 3. Don't follow users that I've followed before and still waiting for follow back
DELETE
--SELECT * 
FROM
	to_follow tf
WHERE
	tf.twitter_id IN
(
		SELECT
			fp.twitter_id
		FROM
			followed_pending_follow_back fp
	);

-- 4. Don't follow users that I've followed before (and been unfollowed) or outside follow back period
DELETE
--SELECT * 
FROM
	to_follow tf
WHERE
	tf.twitter_id IN
(
		SELECT
			fi.twitter_id
		FROM
			followed fi
	);
	
-- 5. Return number of users to follow
SELECT
	COUNT(*)
    INTO
	total
FROM
	to_follow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;
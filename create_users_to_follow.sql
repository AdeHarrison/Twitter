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
	
-- 2. Delete ANY users that I've followed before (and been unfollowed) leaving just new users to follow
DELETE
FROM
	follow f
WHERE
	SELECT
FROM
	follow f
WHERE
	f.twitter_id IN
(
		SELECT
			fol.twitter_id
		FROM
			followed fol
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
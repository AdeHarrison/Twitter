CREATE OR REPLACE
FUNCTION create_users_to_follow ()
RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN

DELETE FROM follow;

-- 1. Select all users that follow me but i don't follow
INSERT INTO follow
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

-- 2. Return number of users to follow
SELECT
	COUNT(*)
    INTO
        total
    FROM
        follow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;
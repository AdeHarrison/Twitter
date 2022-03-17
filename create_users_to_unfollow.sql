CREATE OR REPLACE
FUNCTION create_users_to_unfollow ()
RETURNS integer AS $total$
DECLARE
	total integer;
BEGIN
	
-- 1. Start with ALL users that I follow (friends)
DROP TABLE IF EXISTS tmp_who_i_follow CASCADE;
CREATE TABLE tmp_who_i_follow AS TABLE friend ;
ALTER TABLE tmp_who_i_follow ADD COLUMN unfollow boolean DEFAULT TRUE ;

-- 2. Set unfollow to FALSE for all that match interested terms
CALL keep_friends();

-- 3. Select all users to unfollow
DELETE
FROM
	to_unfollow ;

INSERT
	INTO
	to_unfollow(id,
	"name",
	screen_name,
	description)
SELECT
	id,
	name,
	screen_name,
	description
FROM
	tmp_who_i_follow
WHERE
	unfollow;

-- 4. Delete temp table
DROP TABLE tmp_who_i_follow CASCADE;

SELECT
	count(*)
INTO
	total
FROM
	to_unfollow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;

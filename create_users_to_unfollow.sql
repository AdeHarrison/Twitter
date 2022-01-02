CREATE OR REPLACE
FUNCTION create_users_to_unfollow ()
RETURNS integer AS $total$
DECLARE
	total integer;

BEGIN
	
-- 1. Select ALL users that I follow (friends)
DROP TABLE IF EXISTS tmp_who_i_follow CASCADE;

CREATE TABLE tmp_who_i_follow AS TABLE friend ;

ALTER TABLE tmp_who_i_follow ADD COLUMN unfollow boolean DEFAULT FALSE;

-- 2. Delete ALL users that do not match interested terms
UPDATE
	tmp_who_i_follow
SET
	unfollow = TRUE 
WHERE
NOT(
	lower(description) LIKE '%brexit%'
	OR (lower(description) LIKE '%english%'
		OR lower(name) LIKE '%english%')
	OR (lower(description) LIKE '%gb news%'
		OR lower(name) LIKE '%gb news%')
	OR (lower(description) LIKE '%england%'
		OR lower(name) LIKE '%england%')
	OR (lower(description) LIKE '%brit%'
		OR lower(name) LIKE '%brit%')
	OR (lower(description) LIKE '%army%'
		OR lower(name) LIKE '%army%')
	OR (lower(description) LIKE '%navy%'
		OR lower(name) LIKE '%navy%')
	OR (lower(description) LIKE '%raf%'
		OR lower(name) LIKE '%raf%')
	OR (lower(description) LIKE '%air force%'
		OR lower(name) LIKE '%air force%')
	OR (lower(description) LIKE '%para%'
		OR lower(name) LIKE '%para%')
	OR (lower(description) LIKE '%marine%'
		OR lower(name) LIKE '%marine%')
	OR (lower(description) LIKE '%soldier%'
		OR lower(name) LIKE '%soldier%')
	OR (lower(description) LIKE '%patriot%'
		OR lower(name) LIKE '%patriot%')
	OR (lower(description) LIKE '%military%'
		OR lower(name) LIKE '%military%')
	OR (lower(description) LIKE '%hero%'
		OR lower(name) LIKE '%hero%')
	OR (lower(description) LIKE '%farage%'
		OR lower(name) LIKE '%farage%')
	OR (lower(description) LIKE '%trump%'
		OR lower(name) LIKE '%trump%')
	OR (lower(description) LIKE '%maga%'
		OR lower(name) LIKE '%maga%')
	OR (lower(description) LIKE '%cenotaph%'
		OR lower(name) LIKE '%cenotaph%')
	OR (lower(description) LIKE '%royalist%'
		OR lower(name) LIKE '%royalist%')
	OR (lower(description) LIKE '%reform%'
		OR lower(name) LIKE '%reform%')
	OR (lower(description) LIKE '%gbnew%'
		OR lower(name) LIKE '%gbnew%')
	OR (lower(description) LIKE '%woke%'
		OR lower(name) LIKE '%woke%')
	OR (lower(description) LIKE '%cancel%'
		OR lower(name) LIKE '%cancel%')
	OR (lower(description) LIKE '%blighty%'
		OR lower(name) LIKE '%blighty%')
	OR (lower(description) LIKE '%queen%'
		OR lower(name) LIKE '%queen%')
	OR (lower(description) LIKE '%lgb%'
		OR lower(name) LIKE '%lgb%')
	OR (lower(description) LIKE '%defund%'
		OR lower(name) LIKE '%defund%')
	OR (lower(description) LIKE '%democracy%'
		OR lower(name) LIKE '%democracy%')
	OR (lower(description) LIKE '%conservative%'
		OR lower(name) LIKE '%conservative%')
	OR (lower(description) LIKE '%boris%'
		OR lower(name) LIKE '%boris%')
	OR (lower(description) LIKE '%tory%'
		OR lower(name) LIKE '%tory%')
	OR (lower(description) LIKE '%woke%'
		OR lower(name) LIKE '%woke%')
	OR (lower(description) LIKE '%right%'
		OR lower(name) LIKE '%right%')
	OR (lower(description) LIKE '%football%'
		OR lower(name) LIKE '%football%')
	OR (lower(description) LIKE '%footy%'
		OR lower(name) LIKE '%footy%')
	OR (lower(description) LIKE '%city%'
		OR lower(name) LIKE '%city%')
	OR (lower(description) LIKE '%united%'
		OR lower(name) LIKE '%united%')
	OR (lower(description) LIKE '%utd%'
		OR lower(name) LIKE '%utd%')
	OR (lower(description) LIKE '%rangers%'
		OR lower(name) LIKE '%rangers%')
	OR (lower(description) LIKE '%fc%'
		OR lower(name) LIKE '%fc%')
	OR (lower(description) LIKE '%fuck%'
		OR lower(name) LIKE '%fuck%')
	OR (lower(description) LIKE '%jew%'
		OR lower(name) LIKE '%jew%')
	OR (lower(description) LIKE '%speech%'
		OR lower(name) LIKE '%speech%')
	OR (lower(description) LIKE '%regiment%'
		OR lower(name) LIKE '%regiment%')
	OR (lower(description) LIKE '%whitelivesmatter%'
		OR lower(name) LIKE '%whitelivesmatter%')
	OR (lower(description) LIKE '%leave%'
		OR lower(name) LIKE '%leave%')
			OR (lower(description) LIKE '%offended%'
		OR lower(name) LIKE '%offended%')
	OR (lower(description) LIKE '%hate eu%'
		OR lower(name) LIKE '%hate eu%')
	OR (description = ''
		OR name = '')
	OR (lower(LOCATION) LIKE '%england%'
		OR lower(LOCATION) LIKE '%uk%'
			OR lower(LOCATION) LIKE '%united kingdom%'
				OR lower(LOCATION) LIKE '%london%'
					OR lower(LOCATION) LIKE '%united states%'
						OR lower(LOCATION) LIKE '%usa%'
							OR lower(LOCATION) LIKE '%australia%'
								OR lower(LOCATION) LIKE '%new zealand%')
	OR (description LIKE '%🇬🇧%'
		OR name LIKE '%🇬🇧%')
	OR (description LIKE '%🇬🇬%'
		OR name LIKE '%🇬🇬%')
	OR (description LIKE '%🇺🇸%'
		OR name LIKE '%🇺🇸%')
	OR (description LIKE '%🇦🇺%'
		OR name LIKE '%🇦🇺%')
	OR (description LIKE '%🇨🇦%'
		OR name LIKE '%🇨🇦%')
	OR (description LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%'
		OR name LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%'))
AND NOT unfollow ;

--SELECT * FROM tmp_who_i_follow twif WHERE twif.description LIKE '%🇬🇬%' 
--OR twif.description LIKE '%🇦🇺%'
--OR twif.description LIKE '%🇬🇧%'
--OR twif.description LIKE '%🇮🇱%'
--OR twif.description LIKE '%🇺🇸%'

--3. Unfollow any with limited followers/friends (may have been set to TRUE using terms above) 
--UPDATE
--	tmp_who_i_follow
--SET
--	unfollow = FALSE
--WHERE
--	(followers_count > 200
--		AND friends_count > 200)
--	AND NOT unfollow ;

--SELECT * FROM tmp_who_i_follow twif WHERE 	
--followers_count < 500
--AND friends_count < 500
--	AND unfollow ;


--UPDATE
--	step_1_all_who_i_follow
--SET
--	keep = 'y'
--WHERE
--	ascii(description) > 122;
--
--UPDATE
--	step_1_all_who_i_follow
--SET
--	keep = 'y'
--WHERE
--	(lower(description) LIKE '%lgb%'
--		OR lower(name) LIKE '%lgb%');

DELETE
FROM
	unfollow ;

INSERT
	INTO
	unfollow(twitter_id,
	"name",
	screen_name,
	description)
SELECT
	twitter_id,
	name,
	screen_name,
	description
FROM
	step_1_all_who_i_follow
WHERE
	keep IS NULL;

SELECT
	count(*)
INTO
	total
FROM
	unfollow;

RETURN total;
END;

$total$ LANGUAGE plpgsql;

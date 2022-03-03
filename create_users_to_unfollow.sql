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

UPDATE tmp_who_i_follow SET	unfollow = TRUE;

-- 2. Flag ALL users that do not match interested terms
CALL keep_friend('%brexit%');
CALL keep_friend('%english%');
CALL keep_friend('%gb news%');
CALL keep_friend('%england%');
CALL keep_friend('%brit%');
CALL keep_friend('%army%');
CALL keep_friend('%navy%');
CALL keep_friend('%raf%');
CALL keep_friend('%air force%');
CALL keep_friend('%para%');
CALL keep_friend('%marine%');
CALL keep_friend('%soldier%');
CALL keep_friend('%patriot%');
CALL keep_friend('%nationalist%');
CALL keep_friend('%military%');
CALL keep_friend('%hero%');
CALL keep_friend('%farage%');
CALL keep_friend('%trump%');
CALL keep_friend('%maga%');
CALL keep_friend('%cenotaph%');
CALL keep_friend('%conservative%');
CALL keep_friend('%all lives matter%');
CALL keep_friend('%royalist%');
CALL keep_friend('%reform%');
CALL keep_friend('%gbnew%');
CALL keep_friend('%woke%');
CALL keep_friend('%cancel%');
CALL keep_friend('%blighty%');
CALL keep_friend('%queen%');
CALL keep_friend('%lgb%');
CALL keep_friend('%defund%');
CALL keep_friend('%democracy%');
CALL keep_friend('%conservative%');
CALL keep_friend('%boris%');
CALL keep_friend('%tory%');
CALL keep_friend('%woke%');
CALL keep_friend('%right%');
CALL keep_friend('%football%');
CALL keep_friend('%footy%');
CALL keep_friend('%city%');
CALL keep_friend('%united%');
CALL keep_friend('%utd%');
CALL keep_friend('%rangers%');
CALL keep_friend('%fc%');
CALL keep_friend('%fuck%');
CALL keep_friend('%jew%');
CALL keep_friend('%speech%');
CALL keep_friend('%regiment%');
CALL keep_friend('%whitelivesmatter%');
CALL keep_friend('%leave%');
CALL keep_friend('%offended%');
CALL keep_friend('%christian%');
CALL keep_friend('%hate eu%');
CALL keep_friend('%pc brigade%');
CALL keep_friend('%england%');
CALL keep_friend('%uk%');
CALL keep_friend('%united kingdom%');
CALL keep_friend('%london%');
CALL keep_friend('%united states%');
CALL keep_friend('%usa%');
CALL keep_friend('%australia%');
CALL keep_friend('%new zealand%');
CALL keep_friend('%canada%');
CALL keep_friend('%the south%');
CALL keep_friend('%bridgend%');
CALL keep_friend('%derby%');
CALL keep_friend('%yorkshire%');
CALL keep_friend('%kent%');
CALL keep_friend('%worthing%');
CALL keep_friend('%worcestershire%');
CALL keep_friend('%woking%');
CALL keep_friend('%south east%');
CALL keep_friend('%cambridgeshire%');
CALL keep_friend('%wiltshire%');
CALL keep_friend('%wilburton%');
CALL keep_friend('%wickham%');
CALL keep_friend('%scotland%');
CALL keep_friend('%welling%');
CALL keep_friend('%wednesbury%');
CALL keep_friend('%victoria%');
CALL keep_friend('%glamorgan%');
CALL keep_friend('%ontario%');
CALL keep_friend('%mansfield %');
CALL keep_friend('%wales%');
CALL keep_friend('%sweden%');
CALL keep_friend('%surrey%');
CALL keep_friend('%stoke-on-trent%');
CALL keep_friend('%staffordshire%');
CALL keep_friend('%lancashire.%');
CALL keep_friend('%southend-on-sea%');
CALL keep_friend('%somerset%');
CALL keep_friend('%shropshire%');
CALL keep_friend('%poland%');
CALL keep_friend('%plymouth%');
CALL keep_friend('%not the eu%');
CALL keep_friend('%not in the eu%');
CALL keep_friend('%northumberland%');
CALL keep_friend('%northern ireland%');
CALL keep_friend('%northamptonshire%');
CALL keep_friend('%northampton%');
CALL keep_friend('%north lanarkshire%'); 
CALL keep_friend('%north%');
CALL keep_friend('%norfolk%'); 
CALL keep_friend('%newport%');
CALL keep_friend('%manchester%');
CALL keep_friend('%liverpool%');
CALL keep_friend('%lincolnshire%');
CALL keep_friend('%leicestershire%'); 
CALL keep_friend('%lancashire%');
CALL keep_friend('%isle of wight%');
CALL keep_friend('%island next to autocratic eu%');
CALL keep_friend('%ireland%');
CALL keep_friend('%hertfordshire%');
CALL keep_friend('%harrow%');
CALL keep_friend('%glasgow%'); 
CALL keep_friend('%essex%');
CALL keep_friend('%south east%');
CALL keep_friend('%east sussex%');
CALL keep_friend('%dundee%'); 
CALL keep_friend('%dorset%');
CALL keep_friend('%devon%');
CALL keep_friend('%derbyshire%');
CALL keep_friend('%kent%');
CALL keep_friend('%dartford%');
CALL keep_friend('%coventry%');
CALL keep_friend('%bromley%');
CALL keep_friend('%blackburn%');
CALL keep_friend('%bedfordshire%'); 
CALL keep_friend('%bedford%');

-- 3. Select all users to unfollow
DELETE
FROM
	to_unfollow ;

INSERT
	INTO
	to_unfollow(twitter_id,
	"name",
	screen_name,
	description)
SELECT
	twitter_id,
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

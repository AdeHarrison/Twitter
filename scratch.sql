--DROP TABLE IF EXISTS follow CASCADE;
--DROP TABLE IF EXISTS follower CASCADE;
--DROP TABLE IF EXISTS friend CASCADE;
--DROP TABLE IF EXISTS process_control CASCADE;
--DROP TABLE IF EXISTS unfollow CASCADE;

-- unfollow - done don't delete
--DROP TABLE IF EXISTS users_i_follow_that_do_not_follow_me CASCADE;
--
---- people I follow (friends) that are not followers
--CREATE TABLE users_i_follow_that_do_not_follow_me AS
--select * 
--FROM
--	friend fr
--WHERE
--	fr.twitter_id NOT IN (
--	SELECT
--		fo.twitter_id
--	FROM
--		follower fo);
--
--DELETE
--FROM
--	unfollow;
--
---- people I follow (friends) that are not followers and not fixed who arn't expected to follow me 
--INSERT
--	INTO
--	unfollow
--select *  
--FROM
--	users_i_follow_that_do_not_follow_me fanfm
--WHERE
--	fanfm.twitter_id NOT IN (
--	SELECT
--		f.twitter_id
--	FROM
--		fixed f);

INSERT
	INTO
	followed
SELECT fr.twitter_id , fr.screen_name , fr.created_at 
FROM
	friend fr;



--delete from follow;
--
---- 1. All users that follow me but i don't folow
--insert into follow 
--select * 
--FROM
--	follower fo
--WHERE
--	fo.twitter_id NOT IN (
--	SELECT
--		fr.twitter_id
--	FROM
--		friend fr);

-- Work out ALL users to unfollow in several steps	

-- 1. Copy all who I follow and add a flag column
DROP TABLE IF EXISTS step_1_all_who_i_follow CASCADE;
CREATE TABLE step_1_all_who_i_follow AS TABLE friend ;
ALTER TABLE step_1_all_who_i_follow ADD COLUMN keep VARCHAR(1);

-- Next, work out and delete all friends I want to unfollow

-- 2. Delete ALL users that do not match interested terms
UPDATE step_1_all_who_i_follow SET keep = 'y' where 
lower(description) LIKE '%brexit%'
OR (lower(description) LIKE '%english%' OR lower(name) LIKE '%english%')
OR (lower(description) LIKE '%brit%' OR lower(name) LIKE '%brit%')
OR (lower(description) LIKE '%army%' OR lower(name) LIKE '%army%')
OR (lower(description) LIKE '%navy%' OR lower(name) LIKE '%navy%')
OR (lower(description) LIKE '%raf%' OR lower(name) LIKE '%raf%')
OR (lower(description) LIKE '%air force%' OR lower(name) LIKE '%air force%')
OR (lower(description) LIKE '%para%' OR lower(name) LIKE '%para%')
OR (lower(description) LIKE '%marine%' OR lower(name) LIKE '%marine%')
OR (lower(description) LIKE '%soldier%' OR lower(name) LIKE '%soldier%')
OR (lower(description) LIKE '%patriot%' OR lower(name) LIKE '%patriot%')
OR (lower(description) LIKE '%military%' OR lower(name) LIKE '%military%')
OR (lower(description) LIKE '%hero%' OR lower(name) LIKE '%hero%')
OR (lower(description) LIKE '%farage%' OR lower(name) LIKE '%farage%')
OR (lower(description) LIKE '%trump%' OR lower(name) LIKE '%trump%')
OR (lower(description) LIKE '%maga%' OR lower(name) LIKE '%maga%')
OR (lower(description) LIKE '%cenotaph%' OR lower(name) LIKE '%cenotaph%')
OR (lower(description) LIKE '%royalist%' OR lower(name) LIKE '%royalist%')
OR (lower(description) LIKE '%gb news%' OR lower(name) LIKE '%gb news%')
OR (lower(description) LIKE '%gbnew%' OR lower(name) LIKE '%gbnew%')
OR (lower(description) LIKE '%blighty%' OR lower(name) LIKE '%blighty%')
OR (lower(description) LIKE '%queen%' OR lower(name) LIKE '%queen%')
OR (lower(description) LIKE '%defund%' OR lower(name) LIKE '%defund%')
OR (lower(description) LIKE '%democracy%' OR lower(name) LIKE '%democracy%')
OR (lower(description) LIKE '%conservative%' OR lower(name) LIKE '%conservative%')
OR (lower(description) LIKE '%boris%' OR lower(name) LIKE '%boris%')
OR (lower(description) LIKE '%tory%' OR lower(name) LIKE '%tory%')
OR (lower(description) LIKE '%woke%' OR lower(name) LIKE '%woke%')
OR (lower(description) LIKE '%right%' OR lower(name) LIKE '%right%')
OR (lower(description) LIKE '%football%' OR lower(name) LIKE '%football%')
OR (lower(description) LIKE '%footy%' OR lower(name) LIKE '%footy%')
OR (lower(description) LIKE '%city%' OR lower(name) LIKE '%city%')
OR (lower(description) LIKE '%united%' OR lower(name) LIKE '%united%')
OR (lower(description) LIKE '%utd%' OR lower(name) LIKE '%utd%')
OR (lower(description) LIKE '%rangers%' OR lower(name) LIKE '%rangers%')
OR (lower(description) LIKE '%fc%' OR lower(name) LIKE '%fc%')
OR (lower(description) LIKE '%fuck%' OR lower(name) LIKE '%fuck%')
OR (lower(description) LIKE '%jew%' OR lower(name) LIKE '%jew%')
OR (lower(description) LIKE '%speech%' OR lower(name) LIKE '%speech%')
OR (description = '' OR name = '')
OR (lower(location) LIKE '%england%' OR lower(location) LIKE '%uk%' OR lower(location) LIKE '%united kingdom%' OR lower(location) LIKE '%london%'
OR lower(location) LIKE '%united states%' OR lower(location) LIKE '%usa%'
OR lower(location) LIKE '%australia%' 
OR lower(location) LIKE '%new zealand%')
OR (description LIKE '%🇬🇧%' or name LIKE '%🇬🇧%')
OR (description LIKE '%🇬🇬%' or name LIKE '%🇬🇬%')
OR (description LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' or name LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%');

UPDATE step_1_all_who_i_follow SET keep = 'y' where protected;

UPDATE step_1_all_who_i_follow SET keep = 'y' where (followers_count < 250 and friends_count < 250);

UPDATE step_1_all_who_i_follow SET keep = 'y' where ascii(description) > 122;

UPDATE step_1_all_who_i_follow SET keep = 'y' where (lower(description) LIKE '%lgb%' OR lower(name) LIKE '%lgb%');

delete from unfollow ;

insert into unfollow(twitter_id, "name", screen_name, description)
select twitter_id, name, screen_name,description from step_1_all_who_i_follow where keep is NULL;



SELECT f.twitter_id, count(f.twitter_id) tot FROM follower f GROUP BY f.twitter_id ORDER BY tot DESC ;

select * from step_1_all_who_i_follow f where ascii(description) > 122 and keep is null ;

select * from step_1_all_who_i_follow f where f.followers_count <250 and f.friends_count < 250;

select count(*) save from step_1_all_who_i_follow f where keep ='y';
select count(*) del from step_1_all_who_i_follow f where keep is null;
select * from step_1_all_who_i_follow f where (f.description LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' or f.name LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%');

select * from step_1_all_who_i_follow f where protected;
select * from step_1_all_who_i_follow f where (f.name = '');

select * frqueenom step_1_all_who_i_follow f where (f.description LIKE '%🇬🇧%' or f.name LIKE '%🇬🇧%');
select * from step_1_all_who_i_follow f where (f.description LIKE '%🇬🇬%' or f."name" LIKE '%🇬🇬%');
select * from step_1_all_who_i_follow f where (description LIKE '%󠁧󠁢󠁧󠁿🇺🇸%');
select * from step_1_all_who_i_follow f where (f.description LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' or f.name LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%');



	SELECT 
		* 
	FROM 
		step_1_all_who_i_follow f
	WHERE
NOT lower(f.description) LIKE '%brexit%'
AND NOT (lower(f.description) LIKE '%english%' OR lower(f.name) LIKE '%english%')
AND NOT (lower(f.description) LIKE '%brit%' OR lower(f.name) LIKE '%brit%')
AND NOT (lower(f.description) LIKE '%army%' OR lower(f.name) LIKE '%army%')
AND NOT (lower(f.description) LIKE '%navy%' OR lower(f.name) LIKE '%navy%')
AND NOT (lower(f.description) LIKE '%raf%' OR lower(f.name) LIKE '%raf%')
AND NOT (lower(f.description) LIKE '%para%' OR lower(f.name) LIKE '%para%')
AND NOT (lower(f.description) LIKE '%marine%' OR lower(f.name) LIKE '%marine%')
AND NOT (lower(f.description) LIKE '%marine%' OR lower(f.name) LIKE '%marine%')
AND NOT (lower(f.description) LIKE '%patriot%' OR lower(f.name) LIKE '%patriot%')
AND NOT (lower(f.description) LIKE '%military%' OR lower(f.name) LIKE '%military%')
AND NOT (lower(f.description) LIKE '%farage%' OR lower(f.name) LIKE '%farage%')
AND NOT (lower(f.description) LIKE '%trump%' OR lower(f.name) LIKE '%trump%')
AND NOT (lower(f.description) LIKE '%cenotaph%' OR lower(f.name) LIKE '%cenotaph%')
AND NOT (lower(f.description) LIKE '%royalist%' OR lower(f.name) LIKE '%royalist%')
AND NOT (lower(f.description) LIKE '%gb news%' OR lower(f.name) LIKE '%gb news%')
AND NOT (lower(f.description) LIKE '%gbnew%' OR lower(f.name) LIKE '%gbnew%')
AND NOT (lower(f.description) LIKE '%blighty%' OR lower(f.name) LIKE '%blighty%')
AND NOT (lower(f.description) LIKE '%queen%' OR lower(f.name) LIKE '%queen%')




CREATE TABLE step_1_all_who_i_follow AS TABLE friend ;
DROP TABLE IF EXISTS step_1_all_who_i_follow CASCADE;

CREATE TABLE step_1_users_i_follow_that_do_not_follow_me AS
	SELECT 
		* 
	FROM 
		friend f 
	WHERE
		f.twitter_id NOT IN (
		SELECT 
			fo.twitter_id 
		FROM 
			follower fo 
	)

-- 2. ALL users that I follow that do not match interested termsw
DROP TABLE IF EXISTS step_2_all_users_i_follow_without_interested_terms CASCADE;

CREATE TABLE step_2_all_users_i_follow_without_interested_terms AS
	SELECT 
		* 
	FROM 
		friend f
	WHERE
NOT lower(f.description) LIKE '%brexit%'
AND NOT (lower(f.description) LIKE '%english%' OR lower(f.name) LIKE '%english%')
AND NOT (lower(f.description) LIKE '%brit%' OR lower(f.name) LIKE '%brit%')
AND NOT (lower(f.description) LIKE '%army%' OR lower(f.name) LIKE '%army%')
AND NOT (lower(f.description) LIKE '%navy%' OR lower(f.name) LIKE '%navy%')
AND NOT (lower(f.description) LIKE '%raf%' OR lower(f.name) LIKE '%raf%')
AND NOT (lower(f.description) LIKE '%para%' OR lower(f.name) LIKE '%para%')
AND NOT (lower(f.description) LIKE '%marine%' OR lower(f.name) LIKE '%marine%')
AND NOT (lower(f.description) LIKE '%marine%' OR lower(f.name) LIKE '%marine%')
AND NOT (lower(f.description) LIKE '%patriot%' OR lower(f.name) LIKE '%patriot%')
AND NOT (lower(f.description) LIKE '%military%' OR lower(f.name) LIKE '%military%')
AND NOT (lower(f.description) LIKE '%farage%' OR lower(f.name) LIKE '%farage%')
AND NOT (lower(f.description) LIKE '%trump%' OR lower(f.name) LIKE '%trump%')
AND NOT (lower(f.description) LIKE '%cenotaph%' OR lower(f.name) LIKE '%cenotaph%')
AND NOT (lower(f.description) LIKE '%royalist%' OR lower(f.name) LIKE '%royalist%')
AND NOT (lower(f.description) LIKE '%gb news%' OR lower(f.name) LIKE '%gb news%')
AND NOT (lower(f.description) LIKE '%gbnew%' OR lower(f.name) LIKE '%gbnew%')
AND NOT (lower(f.description) LIKE '%blighty%' OR lower(f.name) LIKE '%blighty%')
AND NOT (lower(f.description) LIKE '%queen%' OR lower(f.name) LIKE '%queen%')
AND NOT (lower(f.description) LIKE '%uk%' OR lower(f.name) LIKE '%uk%')
AND NOT (lower(f.description) LIKE '%🇬🇧%' OR lower(f.name) LIKE '%🇬🇧%')
AND NOT (lower(f.description) LIKE '%🇬🇬%' OR lower(f.name) LIKE '%🇬🇬%')
AND NOT (lower(f.description) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%' OR lower(f.name) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%')
AND NOT (lower(f.description) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' OR lower(f.name) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%')


create table xxx as
	SELECT 
		* 
	FROM 
		friend f
	WHERE
NOT (lower(f.description) LIKE '%🇬🇧%' OR lower(f.name) LIKE '%🇬🇧%')
AND NOT (lower(f.description) LIKE '%🇬🇬%' OR lower(f.name) LIKE '%🇬🇬%')
AND NOT (lower(f.description) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%' OR lower(f.name) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%')
AND NOT (lower(f.description) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' OR lower(f.name) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%')
order by f.screen_name queen

select * from xxx f where f.description LIKE '%🇬🇧%';
select * from friend f where (f.description LIKE '%🇬🇬%');
select * from friend f where (f.description LIKE '%󠁧󠁢󠁧󠁿🇺🇸%');
select * from friend f where (f.description LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%');



-- 2. Work out which other users to unfollow	
	
	
not lower(f.description) LIKE '%brexit%'
-

-- works - don't delete
--DROP TABLE IF EXISTS users_i_follow_that_do_not_follow_me CASCADE;
--
---- Users I follow (friends) that don't follow me
--
--CREATE TABLE users_i_follow_that_do_not_follow_me AS
--SELECT * FROM friend f where
--not lower(f.description) LIKE '%brexit%'
--AND NOT (lower(f.description) LIKE '%english%' OR lower(f.name) LIKE '%english%')
--AND NOT (lower(f.description) LIKE '%brit%' OR lower(f.name) LIKE '%brit%')
--AND NOT (lower(f.description) LIKE '%army%' OR lower(f.name) LIKE '%army%')
--AND NOT (lower(f.description) LIKE '%navy%' OR lower(f.name) LIKE '%navy%')
--AND NOT (lower(f.description) LIKE '%raf%' OR lower(f.name) LIKE '%raf%')
--AND NOT (lower(f.description) LIKE '%para%' OR lower(f.name) LIKE '%para%')
--AND NOT (lower(f.description) LIKE '%marine%' OR lower(f.name) LIKE '%marine%')
--AND NOT (lower(f.description) LIKE '%marine%' OR lower(f.name) LIKE '%marine%')
--AND NOT (lower(f.description) LIKE '%patriot%' OR lower(f.name) LIKE '%patriot%')
--AND NOT (lower(f.description) LIKE '%military%' OR lower(f.name) LIKE '%military%')
--AND NOT (lower(f.description) LIKE '%farage%' OR lower(f.name) LIKE '%farage%')
--AND NOT (lower(f.description) LIKE '%trump%' OR lower(f.name) LIKE '%trump%')
--AND NOT (lower(f.description) LIKE '%cenotaph%' OR lower(f.name) LIKE '%cenotaph%')
--AND NOT (lower(f.description) LIKE '%royalist%' OR lower(f.name) LIKE '%royalist%')
--AND NOT (lower(f.description) LIKE '%gb news%' OR lower(f.name) LIKE '%gb news%')
--AND NOT (lower(f.description) LIKE '%gbnew%' OR lower(f.name) LIKE '%gbnew%')
--AND NOT (lower(f.description) LIKE '%blighty%' OR lower(f.name) LIKE '%blighty%')
--AND NOT (lower(f.description) LIKE '%queen%' OR lower(f.name) LIKE '%queen%')
--AND NOT (lower(f.description) LIKE '%uk%' OR lower(f.name) LIKE '%uk%')
--AND NOT (lower(f.description) LIKE '%🇬🇧%' OR lower(f.name) LIKE '%🇬🇧%')
--AND NOT (lower(f.description) LIKE '%🇬🇬%' OR lower(f.name) LIKE '%🇬🇬%')
--AND NOT (lower(f.description) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%' OR lower(f.name) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%')
--AND NOT (lower(f.description) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' OR lower(f.name) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%')



-- 1. All I follow excluding any with interested terms in description or name
CREATE TABLE users_i_follow_that_do_not_follow_me AS
SELECT * FROM friend f where
not lower(f.description) LIKE '%brexit%'
AND (not lower(f.description) LIKE '%english%' or not lower(f.name) LIKE '%english%')
AND (not lower(f.description) LIKE '%brit%' or not lower(f.name) LIKE '%brit%')
AND (not lower(f.description) LIKE '%army%' or not lower(f.name) LIKE '%army%')
AND (not lower(f.description) LIKE '%navy%' or not lower(f.name) LIKE '%navy%')
AND (not lower(f.description) LIKE '%raf%' or not lower(f.name) LIKE '%raf%')
AND (not lower(f.description) LIKE '%para%' or not lower(f.name) LIKE '%para%')
AND (not lower(f.description) LIKE '%marine%' or not lower(f.name) LIKE '%marine%')
AND (not lower(f.description) LIKE '%marine%' or not lower(f.name) LIKE '%marine%')
AND (not lower(f.description) LIKE '%patriot%' or not lower(f.name) LIKE '%patriot%')
AND (not lower(f.description) LIKE '%military%' or not lower(f.name) LIKE '%military%')
AND (not lower(f.description) LIKE '%farage%' or not lower(f.name) LIKE '%farage%')
AND (not lower(f.description) LIKE '%trump%' or not lower(f.name) LIKE '%trump%')
AND (not lower(f.description) LIKE '%cenotaph%' or not lower(f.name) LIKE '%cenotaph%')
AND (not lower(f.description) LIKE '%royalist%' or not lower(f.name) LIKE '%royalist%')
AND (not lower(f.description) LIKE '%gb news%' or not lower(f.name) LIKE '%gb news%')
AND (not lower(f.description) LIKE '%gbnew%' or not lower(f.name) LIKE '%gbnew%')
AND (not lower(f.description) LIKE '%blighty%' or not lower(f.name) LIKE '%blighty%')
AND (not lower(f.description) LIKE '%queen%' or not lower(f.name) LIKE '%queen%')
AND (not lower(f.description) LIKE '%uk%' or not lower(f.name) LIKE '%uk%')
AND (not lower(f.description) LIKE '%🇬🇧%' or not lower(f.name) LIKE '%🇬🇧%')
AND (not lower(f.description) LIKE '%🇬🇬%' or not lower(f.name) LIKE '%🇬🇬%')
AND (not lower(f.description) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%' or not lower(f.name) LIKE '%󠁧󠁢󠁧󠁿🇺🇸%')
AND (not lower(f.description) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%' or not lower(f.name) LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%')


--CREATE TABLE users_i_follow_that_do_not_follow_me AS
--SELECT f.id, ascii(f.description), f.description FROM friend f where not 
---- Description that doesn't start with either 1-z or A-Z
--((ascii(f.description) >= 65 and ascii(f.description) <= 90) or (ascii(f.description) >= 97 and ascii(f.description) <= 122))
--AND not lower(f.description) LIKE '%brexit%'
--AND not lower(f.description) LIKE '%english%'
--AND not lower(f.description) LIKE '%brit%'
--AND not lower(f.description) LIKE '%army%'
--AND not lower(f.description) LIKE '%navy%'
--AND not lower(f.description) LIKE '%raf%'
--AND not lower(f.description) LIKE '%para%'
--AND not lower(f.description) LIKE '%marine%'
--AND not lower(f.description) LIKE '%woke%'
--AND not lower(f.description) LIKE '%patriot%'
--AND not lower(f.description) LIKE '%military%'
--AND not lower(f.description) LIKE '%farage%'
--AND not lower(f.description) LIKE '%trump%'
--AND not lower(f.description) LIKE '%cenotaph%'
--AND not lower(f.description) LIKE '%royalist%'
--AND not lower(f.description) LIKE '%gb news%'
--AND not lower(f.description) LIKE '%gbnew%'
--AND not lower(f.description) LIKE '%blighty%'
--AND not lower(f.description) LIKE '%queen%'
--AND not lower(f.description) LIKE '%uk%'



--SELECT f.id, ascii(f.description), f.description FROM friend f where not 
---- Description that doesn't start with either 1-z or A-Z
--((ascii(f.description) >= 65 and ascii(f.description) <= 90) or (ascii(f.description) >= 97 and ascii(f.description) <= 122))
--AND not lower(f.description) LIKE '%brexit%'
--AND not lower(f.description) LIKE '%english%'
--AND not lower(f.description) LIKE '%brit%'
--AND not lower(f.description) LIKE '%army%'
--AND not lower(f.description) LIKE '%navy%'
--AND not lower(f.description) LIKE '%raf%'
--AND not lower(f.description) LIKE '%para%'
--AND not lower(f.description) LIKE '%marine%'
--AND not lower(f.description) LIKE '%woke%'
--AND not lower(f.description) LIKE '%patriot%'
--AND not lower(f.description) LIKE '%military%'
--AND not lower(f.description) LIKE '%farage%'
--AND not lower(f.description) LIKE '%trump%'
--AND not lower(f.description) LIKE '%cenotaph%'
--AND not lower(f.description) LIKE '%royalist%'
--AND not lower(f.description) LIKE '%gb news%'
--AND not lower(f.description) LIKE '%gbnew%'
--AND not lower(f.description) LIKE '%blighty%'
--AND not lower(f.description) LIKE '%queen%'
--AND not lower(f.description) LIKE '%uk%'
--AND not f.description LIKE '%🇬🇧%'
--AND not f.description LIKE '%🇬🇬%'
--AND not f.description LIKE '%󠁧󠁢󠁧󠁿🇺🇸%'
--AND not f.description LIKE '%󠁧󠁢󠁥󠁮󠁧󠁿🇮🇱%'


--WHERE
--	(fmanf.followers_count >2500 AND fmanf.friends_count >2500)
--	OR lower(fmanf.description) LIKE '%brexit%'
--	OR lower(fmanf.description) LIKE '%english%'
--	OR lower(fmanf.description) LIKE '%brit%'
--	OR lower(fmanf.description) LIKE '%army%'
--	OR lower(fmanf.description) LIKE '%navy%'
--	OR lower(fmanf.description) LIKE '%raf%'
--	OR lower(fmanf.description) LIKE '%para%'
--	OR lower(fmanf.description) LIKE '%marine%'
--	OR lower(fmanf.description) LIKE '%woke%'
--	OR lower(fmanf.description) LIKE '%patriot%'
--	OR lower(fmanf.description) LIKE '%military%'
--	OR lower(fmanf.description) LIKE '%farage%'
--	OR lower(fmanf.description) LIKE '%trump%'
--	OR lower(fmanf.description) LIKE '%cenotaph%'
--	OR lower(fmanf.description) LIKE '%royalist%'
--	OR lower(fmanf.description) LIKE '%gb news%'
--	OR lower(fmanf.description) LIKE '%gbnew%'
--	OR lower(fmanf.description) LIKE '%blighty%'
--	OR lower(fmanf.description) LIKE '%queen%'
--	OR lower(fmanf.description) LIKE '%uk%'
--	OR lower(fmanf.description) LIKE '%woke%'
--	OR lower(fmanf.description) LIKE '%🇬🇧%';














SELECT *
    FROM users_that_follow_me_and_i_do_not_follow fmanf
    WHERE 
        fmanf.description = ANY('{Brexiteer}'::text[]);

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

SELECT * FROM follower f where f.followers_count <21 and f.friends_count <21 ;
SELECT * FROM friend  f where f.followers_count <21 and f.friends_count <21 ;

select count(f.twitter_id), twitter_id from follower f group by f.twitter_id ;
select * from follower f where twitter_id =538349876;
delete from follower f where twitter_id =538349876;
select * from follower f where twitter_id =1310396731314655237;

delete from ignore_users iu where screen_name in ( 
select screen_name from ignore_users iu where screen_name not in (
'Arron_banks',
'BelindadeLucy',
'CatharineHoey',
'Change_Britain',
'DavidDavisMP',
'DavidGHFrost',
'DefundBBC',
'Digbylj',
'DominicRaab',
'EuroGuido',
'FoundationRigby',
'Fox_Claire',
'GBNEWS',
'GBNfans',
'GuidoFawkes',
'IsabelOakeshott',
'Jacob_Rees_Mogg',
'LeaveEUOfficial',
'LozzaFox',
'MichelleDewbs',
'Nigel_Farage',
'SuzanneEvans1',
'ThatAlexWoman',
'TiceRichard',
'TimMartinWS',
'afneil',
'calvinrobinson',
'darrengrimes_',
'fishingforleave',
'johnredwood',
'labourleave',
'pritipatel',
'reformparty_uk',
'thecarolemalone',
'thecoastguy',
'thereclaimparty',
'tomhfh'));

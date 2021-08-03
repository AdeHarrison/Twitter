-- ONLY run once to create ignore_users table
-- Create ALL ignore_users table than are not following me but I want to still follow
DROP TABLE IF EXISTS ignore_users CASCADE;
CREATE TABLE ignore_users AS
SELECT
	*
FROM
	friend fr
WHERE
	fr.twitter_id NOT IN (
	SELECT
		fo.twitter_id
	FROM
		follower fo);

-- Keep who I follow and don't follow me
delete from ignore_users where name = '';
delete from ignore_users where name = 'Alhabiby Isangyo';
delete from ignore_users where name = 'Andy L';
delete from ignore_users where name = 'B.W. 🙂';
delete from ignore_users where name = 'Ben Harris-Quinney';
delete from ignore_users where name = 'Best_flowers';
delete from ignore_users where name = 'British Centrists 🇬🇧';
delete from ignore_users where name = 'C K';
delete from ignore_users iu where iu.twitter_id = 1124352709480669185;
delete from ignore_users where name = 'Ian Parker';
delete from ignore_users where name = 'Ian🔸';
delete from ignore_users where name = 'Joey';
delete from ignore_users where name = 'Kevin Armstrong';
delete from ignore_users where name = 'MO REESE DELK';
delete from ignore_users where name = 'Misteryo';
delete from ignore_users where name = 'Modou Sanyang';
delete from ignore_users where name = 'Rwnn k’1 fry 🎮';
delete from ignore_users where name = 'Shailendra Kumar Yadav';
delete from ignore_users where name = 'Shakerite 🏴󠁧󠁢󠁥󠁮󠁧󠁿';
delete from ignore_users where name = 'Tricky🇬🇧🇮🇱🇺🇲🏴󠁧󠁢󠁥󠁮󠁧󠁿 🇮🇳';
delete from ignore_users where name = 'Tyler Ottoman';
delete from ignore_users where name = 'UKIP Patriots';
delete from ignore_users where name = 'VMF';
delete from ignore_users where name = 'claybarberxxx';
delete from ignore_users where name = 'get_reallll';
delete from ignore_users where name = 'sk sobuj khan';
delete from ignore_users where name = 'talkRADIO';
delete from ignore_users where name = 'value-rewards';
-- ONLY run once to here

-- Create table of twitter ids Who I follow and don't follows me - 4105
DROP TABLE IF EXISTS following_and_not_follow_me CASCADE;

CREATE TABLE following_and_not_follow_me AS 
SELECT
	*
FROM
	friend fr
WHERE
	fr.twitter_id NOT IN (
	SELECT
		fo.twitter_id
	FROM
		follower fo);

-- Create table of all users to unfollow
DROP TABLE IF EXISTS users_to_unfollow CASCADE;

CREATE TABLE users_to_unfollow AS 
SELECT
	fanfm.twitter_id,
	fanfm."name"
FROM
	following_and_not_follow_me fanfm
WHERE
	fanfm.twitter_id NOT IN (
	SELECT
		iu.twitter_id
	FROM
		ignore_users iu);

-- Create table of twitter ids Who I follow and follows me - 4105
--drop table if exists following_and_follow_me cascade;
--create table following_and_follow_me as 
--SELECT fo.twitter_id, fr.name from follower fo 
--inner join friend fr on fo.twitter_id = fr.twitter_id;

-- Create table of twitter ids Who I follow and don't follows me - 4105
DROP TABLE IF EXISTS following_and_not_follow_me CASCADE;

CREATE TABLE following_and_not_follow_me AS 
SELECT
	fr.twitter_id,
	fr.name
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
	fanfm.name
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

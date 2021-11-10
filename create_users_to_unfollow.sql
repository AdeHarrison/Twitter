-- Create table of twitter ids Who I follow and don't follows me - 4105
DROP TABLE IF EXISTS following_and_not_follow_me CASCADE;

CREATE TABLE following_and_not_follow_me AS 
SELECT
	fr.twitter_id,
	fr.name,
	fr.screen_name 
FROM
	friend fr
WHERE
	fr.twitter_id NOT IN (
	SELECT
		fo.twitter_id
	FROM
		follower fo);

insert into unfollow
SELECT
	fanfm.twitter_id,
	fanfm.name,
	fanfm.screen_name 
FROM
	following_and_not_follow_me fanfm
WHERE
	fanfm.twitter_id NOT IN (
	SELECT
		iu.twitter_id
	FROM
		ignore_users iu);


--	run when friends unfollowed in Twitter
--delete from friend fr where fr.screen_name in (select u.screen_name from unfollow u);

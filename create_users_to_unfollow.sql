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
-- Create table of all users to unfollow
DROP TABLE IF EXISTS users_to_unfollow CASCADE;

CREATE TABLE users_to_unfollow AS 
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

delete from friend fr where fr.screen_name = 'nowokehere';
delete from friend fr where fr.screen_name = 'Jack10167709';
delete from friend fr where fr.screen_name = 'IanParker24';
delete from friend fr where fr.screen_name = 'Sadie200009';
delete from friend fr where fr.screen_name = '_Kate_Barber';
delete from friend fr where fr.screen_name = 'KevinAr63351894';
delete from friend fr where fr.screen_name = 'mrs_valerie_fi2';
delete from friend fr where fr.screen_name = 'PompeyKimmy';
delete from friend fr where fr.screen_name = 'Joey51574402';
-- Create table of twitter ids Who I follow and follows me - 4105
--drop table if exists following_and_follow_me cascade;
--create table following_and_follow_me as 
--SELECT fo.twitter_id, fr.name from follower fo 
--inner join friend fr on fo.twitter_id = fr.twitter_id;

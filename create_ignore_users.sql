-- ONLY run once to create ignore_users table
-- Create ALL ignore_users table than are not following me but I want to still follow
DROP TABLE IF EXISTS ignore_users CASCADE;

CREATE TABLE ignore_users AS
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

-- Keep who I follow and don't follow me
delete from ignore_users where name = '';
delete from ignore_users where name = 'Alhabiby Isangyo';
delete from ignore_users where name = 'Andy L';
delete from ignore_users where name = 'B.W. ðŸ™‚';
delete from ignore_users where name = 'Ben Harris-Quinney';
delete from ignore_users where name = 'Best_flowers';
delete from ignore_users where name = 'British Centrists ðŸ‡¬ðŸ‡§';
delete from ignore_users where name = 'C K';
delete from ignore_users iu where iu.twitter_id = 1124352709480669185;
delete from ignore_users where name = 'Ian Parker';
delete from ignore_users where name = 'IanðŸ”¸';
delete from ignore_users where name = 'Joey';
delete from ignore_users where name = 'Kevin Armstrong';
delete from ignore_users where name = 'MO REESE DELK';
delete from ignore_users where name = 'Misteryo';
delete from ignore_users where name = 'Modou Sanyang';
delete from ignore_users where name = 'Rwnn kâ€™1 fry ðŸŽ®';
delete from ignore_users where name = 'Shailendra Kumar Yadav';
delete from ignore_users where name = 'Shakerite ðŸ?´ó ?§ó ?¢ó ?¥ó ?®ó ?§ó ?¿';
delete from ignore_users where name = 'TrickyðŸ‡¬ðŸ‡§ðŸ‡®ðŸ‡±ðŸ‡ºðŸ‡²ðŸ?´ó ?§ó ?¢ó ?¥ó ?®ó ?§ó ?¿ ðŸ‡®ðŸ‡³';
delete from ignore_users where name = 'Tyler Ottoman';
delete from ignore_users where name = 'UKIP Patriots';
delete from ignore_users where name = 'VMF';
delete from ignore_users where name = 'claybarberxxx';
delete from ignore_users where name = 'get_reallll';
delete from ignore_users where name = 'sk sobuj khan';
delete from ignore_users where name = 'talkRADIO';
delete from ignore_users where name = 'value-rewards';
-- ONLY run once to here


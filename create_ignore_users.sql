-- ONLY run once to create ignore_users table
-- Create ALL ignore_users table than are not following me but I want to still follow
DROP TABLE IF EXISTS ignore_users CASCADE;

CREATE TABLE ignore_users AS
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
delete from ignore_users where name = 'Shakerite �?��?��?��?��?��?��?�';
delete from ignore_users where name = 'Tricky🇬🇧🇮🇱🇺🇲�?��?��?��?��?��?��?� 🇮🇳';
delete from ignore_users where name = 'Tyler Ottoman';
delete from ignore_users where name = 'UKIP Patriots';
delete from ignore_users where name = 'VMF';
delete from ignore_users where name = 'claybarberxxx';
delete from ignore_users where name = 'get_reallll';
delete from ignore_users where name = 'sk sobuj khan';
delete from ignore_users where name = 'talkRADIO';
delete from ignore_users where name = 'value-rewards';

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

--delete from ignore_users iu where iu.twitter_id = 1399477145211133954;
--delete from ignore_users iu where iu.twitter_id = 1342160731366633474;
--delete from ignore_users iu where iu.twitter_id = 1413659112223789056;
--delete from ignore_users iu where iu.twitter_id = 1410339389301997568;
--delete from ignore_users iu where iu.twitter_id = 1346516666;
--delete from ignore_users iu where iu.twitter_id = 1250788752005316609;
--delete from ignore_users iu where iu.twitter_id = 1183327276999544837;
--delete from ignore_users iu where iu.twitter_id = 2993223717;
--delete from ignore_users iu where iu.twitter_id = 1086742378571096064;
--delete from ignore_users iu where iu.twitter_id = 4829910760;
--delete from ignore_users iu where iu.twitter_id = 569505170;
--
--delete from ignore_users iu where iu.screen_name = 'nowokehere';
-- ONLY run once to here


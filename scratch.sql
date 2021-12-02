SELECT *
    FROM follow_me_and_not_following fmanf
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

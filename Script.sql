drop table follower cascade;
drop table friend cascade;


-- who follows me
select count(*) from follower f;

-- who I follow
select count(*) from friend f2;

-- Who I follow and follows me
select count(*) from follower f inner join friend f2 on f.twitter_id = f2.twitter_id;

SELECT * 
FROM follower f
where f.twitter_id not in (select twitter_id  from friend f2);

SELECT * 
FROM friend f2
where f2.twitter_id not in (select twitter_id  from follower f);

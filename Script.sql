drop table follower cascade;
drop table friend cascade;


-- who follows me
select count(*) from follower f;

-- who I follow
select count(*) from friend f2;

-- Who I follow and follows me
select count(*) from follower f inner join friend f2 on f.twitter_id = f2.twitter_id;

create table new_table
as 
select f.twitter_id 
from follower f
   join friend f2 on f.twitter_id = f2.twitter_id ;
  
  

SELECT ISNULL(f.twitter_id,f2.twitter_id) 
FROM follower f
LEFT JOIN friend f2 ON f.twitter_id = f2.twitter_id 
WHERE f.twitter_id  <> f2.twitter_id; 
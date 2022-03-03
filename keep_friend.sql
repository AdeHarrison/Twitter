CREATE OR REPLACE 
PROCEDURE keep_friend(keep varchar(255))
LANGUAGE SQL
AS $$
	UPDATE tmp_who_i_follow SET unfollow = FALSE WHERE(lower("description") LIKE keep);
	UPDATE tmp_who_i_follow SET unfollow = FALSE WHERE(lower("name") LIKE keep);
	UPDATE tmp_who_i_follow SET unfollow = FALSE WHERE(lower("screen_name") LIKE keep);
	UPDATE tmp_who_i_follow SET unfollow = FALSE WHERE(lower("location") LIKE keep);
$$;

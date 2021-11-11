package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Unfollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnfollowRepository extends JpaRepository<Unfollow, Long> {
    void deleteByScreenName(String screenName);
}

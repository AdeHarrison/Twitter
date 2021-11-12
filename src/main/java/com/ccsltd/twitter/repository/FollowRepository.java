package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    void deleteByScreenName(String screenName);
}

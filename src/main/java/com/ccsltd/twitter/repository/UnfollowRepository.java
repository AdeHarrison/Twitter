package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.Unfollow;

@Repository
public interface UnfollowRepository extends JpaRepository<Unfollow, Long> {
    void deleteByScreenName(String screenName);
}

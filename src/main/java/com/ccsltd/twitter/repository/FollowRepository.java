package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    void deleteByScreenName(String screenName);
}

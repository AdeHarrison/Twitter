package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.FollowIgnore;

@Repository
public interface FollowIgnoreRepository extends JpaRepository<FollowIgnore, Long> {
    void deleteByScreenName(String screenName);
}

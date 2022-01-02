package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.Unfollowed;

@Repository
public interface UnfollowedRepository extends JpaRepository<Unfollowed, Long> {
    void deleteByScreenName(String screenName);
}

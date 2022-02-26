package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.Followed;

@Repository
public interface FollowedRepository extends JpaRepository<Followed, Long> {
    void deleteByScreenName(String screenName);
}

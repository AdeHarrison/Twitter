package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.ToUnfollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToUnfollowRepository extends JpaRepository<ToUnfollow, Long> {
    void deleteByScreenName(String screenName);
}

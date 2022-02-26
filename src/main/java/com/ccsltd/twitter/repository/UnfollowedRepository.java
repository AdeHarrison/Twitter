package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.UnFollowed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnfollowedRepository extends JpaRepository<UnFollowed, Long> {
    void deleteByScreenName(String screenName);
}

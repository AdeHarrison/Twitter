package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.ToFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToFollowRepository extends JpaRepository<ToFollow, Long> {
    void deleteByScreenName(String screenName);
}

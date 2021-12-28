package com.ccsltd.twitter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.Followed;
import com.ccsltd.twitter.entity.Follower;

@Repository
public interface FollowedRepository extends JpaRepository<Follower, Long> {
    Optional<Followed> findByTwitterId(Long id);

    void deleteByScreenName(String screenName);
}

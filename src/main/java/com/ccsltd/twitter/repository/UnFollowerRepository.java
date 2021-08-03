package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.UnFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnFollowerRepository extends JpaRepository<UnFollower, Long> {
}

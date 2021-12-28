package com.ccsltd.twitter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.FollowPending;
import com.ccsltd.twitter.entity.Followed;

@Repository
public interface FollowPendingRepository extends JpaRepository<FollowPending, Long> {
    Optional<FollowPending> findByTwitterId(Long id);
}

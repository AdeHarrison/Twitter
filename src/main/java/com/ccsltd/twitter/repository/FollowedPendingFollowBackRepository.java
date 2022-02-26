package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.FollowedPendingFollowBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowedPendingFollowBackRepository extends JpaRepository<FollowedPendingFollowBack, Long> {
    Optional<FollowedPendingFollowBack> findByTwitterId(Long id);
}

package com.ccsltd.twitter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.FollowIgnore;

@Repository
public interface FollowIgnoreRepository extends JpaRepository<FollowIgnore, Long> {
    Optional<FollowIgnore> findByTwitterId(Long id);
}

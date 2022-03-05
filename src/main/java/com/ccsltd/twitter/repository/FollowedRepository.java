package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Followed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowedRepository extends JpaRepository<Followed, Long> {
    Optional<Followed> findByTwitterId(Long id);

    void deleteByScreenName(String screenName);
}

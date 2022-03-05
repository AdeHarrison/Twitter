package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.UnFollowed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnfollowedRepository extends JpaRepository<UnFollowed, Long> {
    Optional<UnFollowed> findByTwitterId(Long id);

    void deleteByScreenName(String screenName);
}

package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByTwitterId(Long id);

    void deleteByScreenName(String screenName);
}

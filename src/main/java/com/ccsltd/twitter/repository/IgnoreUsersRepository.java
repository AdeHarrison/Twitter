package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Unfollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IgnoreUsersRepository extends JpaRepository<Unfollow, Long> {
}

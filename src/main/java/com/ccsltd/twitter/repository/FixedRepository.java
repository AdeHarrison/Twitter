package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.Fixed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedRepository extends JpaRepository<Fixed, Long> {
}

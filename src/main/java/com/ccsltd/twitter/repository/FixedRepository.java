package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccsltd.twitter.entity.Fixed;

@Repository
public interface FixedRepository extends JpaRepository<Fixed, Long> {
}

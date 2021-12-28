package com.ccsltd.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ccsltd.twitter.entity.ProcessControl;

public interface ProcessControlRepository extends JpaRepository<ProcessControl, Long> {
    ProcessControl findByProcess(String process);
}

package com.ccsltd.twitter.repository;

import com.ccsltd.twitter.entity.ProcessControl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessControlRepository extends JpaRepository<ProcessControl, Long> {
    ProcessControl findByProcess(String process);
}

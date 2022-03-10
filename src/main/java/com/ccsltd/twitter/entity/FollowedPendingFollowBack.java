package com.ccsltd.twitter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class FollowedPendingFollowBack implements Serializable {

    public FollowedPendingFollowBack(Long id, String screenName) {
        this.id = id;
        this.screenName = screenName;
    }

    @Id
    private Long id;

    @Column
    private String screenName;

    @CreationTimestamp
    private LocalDateTime timeStamp;
}

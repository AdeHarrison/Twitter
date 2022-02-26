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

    public FollowedPendingFollowBack(Long twitterId, String screenName) {
        this.twitterId = twitterId;
        this.screenName = screenName;
    }

    @Id
    private Long twitterId;

    @Column
    private String screenName;

    @CreationTimestamp
    private LocalDateTime timeStamp;
}

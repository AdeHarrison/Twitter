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
public class UnFollowed implements Serializable {

    public UnFollowed(Long twitterId, String screenName) {
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

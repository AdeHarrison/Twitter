package com.ccsltd.twitter.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
public class FollowPending implements Serializable {

    public FollowPending(Long twitterId, String screenName) {
        this.twitterId = twitterId;
        this.screenName = screenName;
    }

    @Id
    private final Long twitterId;

    @Column
    private final String screenName;

    @CreationTimestamp
    private Date timeStamp;
}

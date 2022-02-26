package com.ccsltd.twitter.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Followed implements Serializable {

    public Followed(Long twitterId, String screenName) {
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
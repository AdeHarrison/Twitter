package com.ccsltd.twitter.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Follower {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private Long twitterId;

    @Column
    private String screenName;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String location;

    @Column
    private Integer followersCount;

    @Column
    private Integer friendsCount;
}

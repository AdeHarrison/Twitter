package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Friend {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long twitterId;

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

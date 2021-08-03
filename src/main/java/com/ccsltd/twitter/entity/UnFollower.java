package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

//@AllArgsConstructor
//@NoArgsConstructor
@Data
//@Builder
@Entity(name = "users_to_unfollow")
public class UnFollower {

    @Id
    private Long twitterId;

    @Column
    private String name;
}

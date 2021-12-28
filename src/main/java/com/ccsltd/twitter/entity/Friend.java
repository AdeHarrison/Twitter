package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Friend implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long twitterId;

    @Column
    private String name;

    @Column
    private String screenName;

    @Column
    private String location;

    @Column
    private String description;

    @Column(name = "protected")
    private Boolean protectedTweets;

    @Column
    private Boolean verified;

    @Column
    private Integer followersCount;

    @Column
    private Integer friendsCount;

    @Column
    private LocalDateTime created_at;
}

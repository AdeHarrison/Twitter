package com.ccsltd.twitter.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NamedStoredProcedureQuery(name = "createUsersToFollow", procedureName = "create_users_to_follow")

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Follow implements Serializable {

    @Id
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

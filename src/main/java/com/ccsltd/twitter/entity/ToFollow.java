package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@NamedStoredProcedureQuery(name = "createUsersToFollow", procedureName = "create_users_to_follow")

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class ToFollow implements Serializable {

    @Id
    private Long id;

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

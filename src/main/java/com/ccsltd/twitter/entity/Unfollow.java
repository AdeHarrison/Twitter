package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@NamedStoredProcedureQuery(
        name = "createUnfollow",
        procedureName = "create_unfollow"
)

@NamedStoredProcedureQuery(
        name = "createFollow",
        procedureName = "create_follow"
)

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Unfollow implements Serializable {

    @Id
    private Long twitterId;

    @Column
    private Long id;

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

    @Column(name = "protected")
    private Boolean protectedTweets;
}

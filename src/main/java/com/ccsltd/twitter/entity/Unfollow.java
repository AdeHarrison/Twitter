package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@NamedStoredProcedureQuery(
        name = "createUnfollow",
        procedureName = "create_users_to_unfollow"
)

@NamedStoredProcedureQuery(
        name = "createFollow",
        procedureName = "create_users_to_follow"
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
    private String name;

    @Column
    private String screenName;

    @Column
    private String description;
}

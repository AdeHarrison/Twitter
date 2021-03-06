package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import java.io.Serializable;

@NamedStoredProcedureQuery(
        name = "createUsersToUnfollow",
        procedureName = "create_users_to_unfollow"
)

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class ToUnfollow implements Serializable {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String screenName;

    @Column
    private String description;
}

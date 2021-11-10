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

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Unfollow implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private Long twitterId;

    @Column
    private String screenName;

    @Column
    private String name;
}

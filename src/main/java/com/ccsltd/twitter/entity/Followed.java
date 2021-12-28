package com.ccsltd.twitter.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Entity
public class Followed implements Serializable {

    @Id
    private Long twitterId;

    @Column
    private String screenName;
}

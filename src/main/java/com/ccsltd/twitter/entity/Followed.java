package com.ccsltd.twitter.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@Entity
public class Followed implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private Long twitterId;

    @Column
    private String screenName;
}

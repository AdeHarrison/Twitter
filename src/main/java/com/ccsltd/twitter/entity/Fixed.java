package com.ccsltd.twitter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Fixed implements Serializable {

    @Id
    private Long id;

    @Column
    private String screenName;

    @Column
    private String name;
}
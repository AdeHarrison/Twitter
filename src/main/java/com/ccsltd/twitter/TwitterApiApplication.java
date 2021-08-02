package com.ccsltd.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// todo sort this
@EnableJpaRepositories(basePackages = "com.ccsltd.twitter.repository")
@EntityScan(basePackages = "com.ccsltd.twitter.entity")
public class TwitterApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitterApiApplication.class, args);
    }

}

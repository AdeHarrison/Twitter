package com.ccsltd.twitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import static java.lang.System.getenv;

@Component
public class Config {

    @Bean
    public Twitter getTwitter() {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(Boolean.parseBoolean(getenv("twitter4j.debug")))
                .setOAuthConsumerKey(getenv("twitter4j.oauth.consumerKey"))
                .setOAuthConsumerSecret(getenv("twitter4j.oauth.consumerSecret"))
                .setOAuthAccessToken(getenv("twitter4j.oauth.accessToken"))
                .setOAuthAccessTokenSecret(getenv("twitter4j.oauth.accessTokenSecret"));

        return new TwitterFactory(cb.build()).getInstance();
    }
}

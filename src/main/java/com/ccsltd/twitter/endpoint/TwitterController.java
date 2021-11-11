package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.entity.Unfollow;
import com.ccsltd.twitter.service.TwitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "twitter")
public class TwitterController {

    private final TwitterService twitterService;

    @GetMapping
    public String homePage() {
        return "My Twitter Home Page";
    }

    @GetMapping(path = "/initialise")
    public String initialiseData(@RequestParam(name = "status", required = true) String status) {
        return twitterService.initialise(status);
    }

    @GetMapping(path = "/unfollow")
    public List<Unfollow> unfollow() throws TwitterException {
        return twitterService.unfollow();
    }

    @GetMapping(path = "/deserialize")
    public String deserialize() {
        return twitterService.deserialize();
    }

    @GetMapping(path = "/refresh")
    public String refreshData() {
        return twitterService.refresh();
    }
}

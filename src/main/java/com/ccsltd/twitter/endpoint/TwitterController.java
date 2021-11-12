package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.entity.Follow;
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

    @GetMapping(path = "/reset")
    public String reset(@RequestParam(name = "to") String resetTo) {
        return twitterService.reset(resetTo);
    }

    @GetMapping(path = "/unfollow")
    public List<Unfollow> unfollow() throws TwitterException {
        return twitterService.unfollow();
    }

    @GetMapping(path = "/follow")
    public List<Follow> follow() throws TwitterException {
        return twitterService.follow();
    }

    @GetMapping(path = "/snapshot")
    public String snapshot(@RequestParam(name = "to") String snapshotTo) {
        return twitterService.snapshot(snapshotTo);
    }

    @GetMapping(path = "/refresh")
    public String refreshData() {
        return twitterService.refresh();
    }
}

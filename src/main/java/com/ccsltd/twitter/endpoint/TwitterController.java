package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.service.TwitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;

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

    @GetMapping(path = "/identify-followers")
    public String identifyFollowers() throws TwitterException {
        return twitterService.identifyUsersToFollow();
    }

    @GetMapping(path = "/follow")
    public String follow() throws TwitterException {
        return twitterService.follow();
    }

    @GetMapping(path = "/identify-unfollowers")
    public String identifyUnfollowers() throws TwitterException {
        return twitterService.identifyUsersToUnfollow();
    }

    @GetMapping(path = "/unfollow")
    public String unfollow() throws TwitterException {
        return twitterService.unfollow();
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

package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.service.TwitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
//@RequestMapping(path = "twitter")
public class TwitterController {

    private final TwitterService twitterService;

    @GetMapping
    public String homePage() {
        return "My Twitter Home Page";
    }

    @GetMapping(path = "/initialise")
    public String initialiseData() {
        return twitterService.initialise();
    }

    @GetMapping(path = "/load-all-followers")
    public String loadAllFollowers() {
        return twitterService.loadAllFollowers();
    }
    @GetMapping(path = "/load-all-friends")
    public String loadAllFriends() {
        return twitterService.loadAllFriends();
    }

    @GetMapping(path = "/reset")
    public String reset(@RequestParam(name = "to") String resetTo) {
        return twitterService.reset(resetTo);
    }

    @GetMapping(path = "/snapshot")
    public String snapshot(@RequestParam(name = "to") String snapshotTo) {
        return twitterService.snapshot(snapshotTo);
    }

    @GetMapping(path = "/create-new-followers-and-friends")
    public String createNewFollowersAndFriends() {
        return twitterService.createNewFollowersAndFriends();
    }

    @GetMapping(path = "/create-new-followers")
    public String createNewFollowers() {
        return twitterService.createNewFollowers();
    }

    @GetMapping(path = "/create-new-friends")
    public String createNewFriends() {
        return twitterService.createNewFriends();
    }
}

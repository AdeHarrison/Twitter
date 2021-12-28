package com.ccsltd.twitter.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ccsltd.twitter.service.UnfollowService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "unfollow")
public class UnfollowController {

    private final UnfollowService unfollowService;

    @GetMapping(path = "/identify-unfollowers")
    public String identifyUnfollowers() {
        return unfollowService.identifyUsersToUnfollow();
    }

    @GetMapping(path = "/unfollow")
    public String unfollow() {
        return unfollowService.unfollow();
    }
}

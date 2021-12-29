package com.ccsltd.twitter.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ccsltd.twitter.service.FollowService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "follow")
public class FollowController {

    private final FollowService followService;

    @GetMapping(path = "/identify-followers")
    public String identifyFollowers() {
        return followService.identifyUsersToFollow();
    }

    @GetMapping(path = "/follow")
    public String follow() {
        return followService.follow();
    }
}

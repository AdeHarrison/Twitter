package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.service.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "following")
public class FollowingController {

    private final FollowerService followerService;

    @GetMapping(path = "/list")
    public List<Friend> getFollowers() {
        return followerService.getFollowing();
    }
}

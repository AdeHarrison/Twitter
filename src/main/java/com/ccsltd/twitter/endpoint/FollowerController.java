package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.service.FollowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RequiredArgsConstructor
@RestController
@RequestMapping(path = "followers")
public class FollowerController {

    private final FollowerService followerService;

    @Autowired
    public FollowerController(FollowerService followerService) {
        this.followerService = followerService;
    }

    @GetMapping(path = "/list")
    public List<Follower> getFollowers() {
        return followerService.getFollowers();
    }
}

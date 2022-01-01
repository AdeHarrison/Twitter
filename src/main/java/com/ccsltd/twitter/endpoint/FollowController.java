package com.ccsltd.twitter.endpoint;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ccsltd.twitter.service.FollowService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "follow")
public class FollowController {

    private Logger log = LoggerFactory.getLogger(FollowController.class);

    private final FollowService followService;

    @GetMapping(path = "/identify-follows")
    public String identifyFollows() {
        String logMessage = format("'%s' Users to Follow", followService.identifyFollows());

        log.info(logMessage);

        return logMessage;
    }

    @GetMapping(path = "/follow")
    public String follow(@RequestParam(name = "followLimit", required = false, defaultValue = "30") int followLimit) {
        String logMessage = format("'%s' User(s) remain to follow", followService.follow(followLimit));

        log.info(logMessage);

        return logMessage;
    }
}

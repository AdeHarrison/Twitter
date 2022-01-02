package com.ccsltd.twitter.endpoint;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ccsltd.twitter.service.UnfollowService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "unfollow")
public class UnfollowController {

    private Logger log = LoggerFactory.getLogger(UnfollowController.class);

    private final UnfollowService unfollowService;

    @GetMapping(path = "/identify-unfollows")
    public String identifyUnfollows() {
        String logMessage = format("'%s' Users to Unfollow", unfollowService.identifyUnfollows());

        log.info(logMessage);

        return logMessage;
    }

    @GetMapping(path = "/unfollow")
    public String unfollow(
            @RequestParam(name = "unFollowLimit", required = false, defaultValue = "30") int unFollowLimit) {
        String logMessage = format("'%s' Users remain to unfollow", unfollowService.unfollow(unFollowLimit));

        log.info(logMessage);

        return logMessage;
    }
}

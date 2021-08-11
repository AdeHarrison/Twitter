package com.ccsltd.twitter.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ccsltd.twitter.service.TwitterService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "twitter")
public class TwitterController {

    private final TwitterService twitterService;

    @GetMapping(path = "/initialise")
    public String initialiseData() {
        return twitterService.initialiseData();
    }

    @GetMapping(path = "/refresh")
    public String refreshData() {
        return twitterService.refreshData();
    }
}

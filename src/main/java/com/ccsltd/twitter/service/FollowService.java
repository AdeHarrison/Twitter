package com.ccsltd.twitter.service;

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ccsltd.twitter.entity.Follow;
import com.ccsltd.twitter.entity.FollowPending;
import com.ccsltd.twitter.repository.FollowPendingRepository;
import com.ccsltd.twitter.repository.FollowRepository;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@RequiredArgsConstructor
@Slf4j
@Service
public class FollowService {

    private final Twitter twitter;
    private final FollowerRepository followerRepository;
    private final FollowRepository followRepository;
    private final FollowPendingRepository followPendingRepository;
    private final EntityManager manager;

    public String identifyUsersToFollow() {
        StoredProcedureQuery followFunction = manager.createNamedStoredProcedureQuery("createUsersToFollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        followFunction.execute();
        Integer followCount = (Integer) followFunction.getOutputParameterValue("followCount");

        String logMessage = format("'%s' Users to Follow", followCount);

        log.info(logMessage);

        return logMessage;
    }

    @Transactional
    public String follow() {
        List<Follow> allToFollow = followRepository.findAll();

        Consumer<Follow> createFriendship = user -> {
            String screenName = user.getScreenName();
            boolean done = false;
            int rateLimitCount = 1;
            int sleptForSecondsTotal = 0;

            while (!done) {
                try {
                    twitter.createFriendship(screenName);
                    followRepository.deleteByScreenName(user.getScreenName());
                    done = true;
                    log.info("followed '{}'", screenName);
                } catch (TwitterException te) {

                    switch (te.getErrorCode()) {

                    // User not found
                    case 108:
                        followerRepository.deleteByScreenName(user.getScreenName());
                        followRepository.deleteByScreenName(user.getScreenName());
                        done = true;
                        log.info("User doesn't exist '{}'", screenName);
                        return;

                    // User followed already requested
                    case 160:
                        Optional<FollowPending> followPending = followPendingRepository.findByTwitterId(
                                user.getTwitterId());

                        if (followPending.isPresent()) {
                            LocalDateTime createdDate = Utils.convertDateToLocalDateTime(
                                    followPending.get().getTimeStamp());
                            LocalDateTime cutOffDate = LocalDateTime.now().minusDays(5L);

                            if (createdDate.isBefore(cutOffDate)) {

                            }
                        } else {
                            followPendingRepository.save(new FollowPending(user.getTwitterId(), screenName));
                        }

                        followerRepository.deleteByScreenName(user.getScreenName());
                        followRepository.deleteByScreenName(user.getScreenName());
                        done = true;
                        log.info("Already requested to follow '{}'", screenName);
                        return;

                    // User follow rate limit reached
                    case 161:
                        log.info("Failed to follow '{}', Follow limit reached - try later", screenName);
                        return;

                    default:
                        log.info("Unhandled error code '{}'", te.getErrorCode());
                        Utils.handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                        sleptForSecondsTotal += Utils.SLEEP_SECONDS;
                    }
                }
            }
        };

        allToFollow.forEach(createFriendship);

        String logMessage = format("'%s' Users remain to follow", followRepository.findAll().size());

        log.info(logMessage);

        return logMessage;
    }

    private String createFileName(String filenameFormat, String resetTo) {
        return format(filenameFormat, resetTo);
    }
}


package com.ccsltd.twitter.service;

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
import com.ccsltd.twitter.entity.FollowIgnore;
import com.ccsltd.twitter.entity.FollowPending;
import com.ccsltd.twitter.repository.FollowIgnoreRepository;
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

    public static final long EXPIRY_DAYS = 5L;

    private final Twitter twitter;
    private final FollowerRepository followerRepository;
    private final FollowRepository followRepository;
    private final FollowPendingRepository followPendingRepository;
    private final FollowIgnoreRepository followIgnoreRepository;
    private final EntityManager manager;
    private final Utils utils;

    public int identifyFollows() {
        StoredProcedureQuery followFunction = manager.createNamedStoredProcedureQuery("createUsersToFollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        followFunction.execute();

        return (int) followFunction.getOutputParameterValue("followCount");
    }

    @Transactional
    public int follow(final int followLimit) {
        List<Follow> allFollowList = followRepository.findAll();
        int maxToFollow = allFollowList.size();
        int actualToFollow = (maxToFollow >= followLimit ? followLimit : maxToFollow);

        List<Follow> followList = allFollowList.subList(0, actualToFollow);

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
                        followIgnoreRepository.deleteByScreenName(user.getScreenName());
                        done = true;
                        log.info("User doesn't exist '{}'", screenName);
                        return;

                    // User followed already requested
                    case 160:
                        Optional<FollowPending> followPending = followPendingRepository.findByTwitterId(
                                user.getTwitterId());

                        if (followPending.isPresent()) {
                            LocalDateTime createdDate = followPending.get().getTimeStamp();
                            LocalDateTime cutOffDate = LocalDateTime.now().minusDays(EXPIRY_DAYS);

                            if (createdDate.isBefore(cutOffDate)) {
                                followIgnoreRepository.save(new FollowIgnore(user.getTwitterId(), screenName));

                                log.info("Already requested to follow '{}' and request date '{}' has expired",
                                        screenName, createdDate);
                            } else {
                                log.info("Already requested to follow '{}' and request date '{}' is still active",
                                        screenName, createdDate);
                            }
                        } else {
                            followPendingRepository.save(new FollowPending(user.getTwitterId(), screenName));
                            log.info("Already requested to follow '{}' and created new tracking record", screenName);
                        }

                        followRepository.deleteByScreenName(screenName);

                        //     ????                   followerRepository.deleteByScreenName(user.getScreenName());
                        done = true;
                        return;

                    // User follow rate limit reached
                    case 161:
                        log.info("Failed to follow '{}', Follow limit reached - try later", screenName);
                        utils.handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                        sleptForSecondsTotal += Utils.SLEEP_SECONDS;
                        return;

                    default:
                        log.info("Unhandled error code '{}'", te.getErrorCode());
                        return;
                    }
                }
            }
        };

        followList.forEach(createFriendship);

        return followRepository.findAll().size();
    }
}


package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.Followed;
import com.ccsltd.twitter.entity.FollowedPendingFollowBack;
import com.ccsltd.twitter.entity.ToFollow;
import com.ccsltd.twitter.repository.FollowedPendingFollowBackRepository;
import com.ccsltd.twitter.repository.FollowedRepository;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.ToFollowRepository;
import com.ccsltd.twitter.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.ccsltd.twitter.service.Constant.*;
import static com.ccsltd.twitter.utils.Utils.sleepForSeconds;

@RequiredArgsConstructor
@Slf4j
@Service
public class FollowService {

    public static final long EXPIRY_DAYS = 5L;

    private final Twitter twitter;
    private final FollowerRepository followerRepository;
    private final ToFollowRepository toFollowRepository;
    private final FollowedPendingFollowBackRepository followedPendingFollowBackRepository;
    private final FollowedRepository followedRepository;
    private final EntityManager manager;
    private final Utils utils;

    public int identifyFollows() {
        StoredProcedureQuery followFunction = manager.createNamedStoredProcedureQuery("createUsersToFollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        followFunction.execute();

        return (int) followFunction.getOutputParameterValue("followCount");
    }

    @Transactional
    public int follow(int followLimit) {
        List<ToFollow> allToFollowList = toFollowRepository.findAll();
        int maxToFollow = allToFollowList.size();
        int actualToFollow = (maxToFollow >= followLimit ? followLimit : maxToFollow);

        List<ToFollow> toFollowList = allToFollowList.subList(0, actualToFollow);

        Consumer<ToFollow> createFriendship = user -> {
            String screenName = user.getScreenName();

            try {
                twitter.createFriendship(screenName);
                followedRepository.save(new Followed(user.getTwitterId(), screenName));
                toFollowRepository.deleteByScreenName(screenName);
                sleepForSeconds(1);
                log.info("followed '{}'", screenName);
                return;
            } catch (TwitterException te) {

                switch (te.getErrorCode()) {

                    case INVALID_TOKEN:
                        log.info("Invalid Token");
                        return;

                    case USER_NOT_FOUND:
                        followerRepository.deleteByScreenName(screenName);
                        toFollowRepository.deleteByScreenName(screenName);
                        followedRepository.deleteByScreenName(screenName);
                        log.info("User doesn't exist '{}'", screenName);
                        return;

                    case FOLLOW_ALREADY_REQUESTED:
                        Optional<FollowedPendingFollowBack> followPending = followedPendingFollowBackRepository.findByTwitterId(
                                user.getTwitterId());

                        if (followPending.isPresent()) {
                            LocalDateTime createdDate = followPending.get().getTimeStamp();
                            LocalDateTime cutOffDate = LocalDateTime.now().minusDays(EXPIRY_DAYS);

                            if (createdDate.isBefore(cutOffDate)) {
                                followedRepository.save(new Followed(user.getTwitterId(), screenName));

                                log.info("Already requested to follow '{}' and request date '{}' has expired", screenName,
                                        createdDate);
                            } else {
                                log.info("Already requested to follow '{}' and request date '{}' is still active",
                                        screenName, createdDate);
                            }
                        } else {
                            followedPendingFollowBackRepository.save(new FollowedPendingFollowBack(user.getTwitterId(), screenName));
                            log.info("Already requested to follow '{}' and created new tracking record", screenName);
                        }

                        toFollowRepository.deleteByScreenName(screenName);

                        return;

                    case RATE_LIMIT_REACHED:
                        log.info("Failed to follow '{}', Follow limit reached - try later", screenName);
                        sleepForSeconds(20);
                        return;

                    default:
                        log.info("Unhandled error code '{}'", te.getErrorCode());
                        return;
                }
            }
        };

        toFollowList.forEach(createFriendship);

        return toFollowRepository.findAll().size();
    }
}


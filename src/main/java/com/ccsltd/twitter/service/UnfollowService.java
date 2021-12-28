package com.ccsltd.twitter.service;

import static java.lang.String.format;

import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ccsltd.twitter.entity.Unfollow;
import com.ccsltd.twitter.repository.FollowRepository;
import com.ccsltd.twitter.repository.FriendRepository;
import com.ccsltd.twitter.repository.UnfollowRepository;
import com.ccsltd.twitter.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@RequiredArgsConstructor
@Slf4j
@Service
public class UnfollowService {

    private final Twitter twitter;
    private final FriendRepository friendRepository;
    private final UnfollowRepository unfollowRepository;
    private final FollowRepository followRepository;
    private final EntityManager manager;

    private final int SLEEP_SECONDS = 60;

    public String identifyUsersToUnfollow() {

        if (!checkSafeToUnfollow()) {
            String logMessage = format("'%s' Users must be followed first", followRepository.findAll().size());

            log.info(logMessage);

            return logMessage;
        }

        StoredProcedureQuery unfollowFunction = manager.createNamedStoredProcedureQuery("createUsersToUnfollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        unfollowFunction.execute();
        Integer unfollowCount = (Integer) unfollowFunction.getOutputParameterValue("followCount");

        String logMessage = format("'%s' Users to Unfollow", unfollowCount);

        log.info(logMessage);

        return logMessage;
    }

    @Transactional
    public String unfollow() {
        if (!checkSafeToUnfollow()) {
            String logMessage = format("'%s' Users must be followed first", followRepository.findAll().size());

            log.info(logMessage);

            return logMessage;
        }

        List<Unfollow> allToUnfollow = unfollowRepository.findAll();

        Consumer<Unfollow> unfollowFriend = user -> {
            String screenName = user.getScreenName();
            boolean done = false;
            int rateLimitCount = 1;
            int sleptForSecondsTotal = 0;

            while (!done) {
                try {
                    twitter.destroyFriendship(screenName);
                    unfollowRepository.deleteByScreenName(screenName);
                    friendRepository.deleteByScreenName(screenName);
                    done = true;

                    log.info("unfollowed '{}' ", screenName);
                } catch (TwitterException te) {
                    if (te.getErrorCode() == 34) {
                        unfollowRepository.deleteByScreenName(screenName);
                        friendRepository.deleteByScreenName(screenName);
                        done = true;
                        break;
                    } else {
                        Utils.handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                        sleptForSecondsTotal += SLEEP_SECONDS;
                    }
                }
            }
        };

        allToUnfollow.forEach(unfollowFriend);

        String logMessage = format("'%s' Users remain to unfollow", unfollowRepository.findAll().size());

        log.info(logMessage);

        return logMessage;
    }

    private boolean checkSafeToUnfollow() {
        return followRepository.findAll().size() == 0;
    }

    private String createFileName(String filenameFormat, String resetTo) {
        return format(filenameFormat, resetTo);
    }
}


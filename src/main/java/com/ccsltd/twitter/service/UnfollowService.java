package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.ToUnfollow;
import com.ccsltd.twitter.entity.UnFollowed;
import com.ccsltd.twitter.repository.FriendRepository;
import com.ccsltd.twitter.repository.ToFollowRepository;
import com.ccsltd.twitter.repository.ToUnfollowRepository;
import com.ccsltd.twitter.repository.UnfollowedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.ccsltd.twitter.service.Constant.INVALID_TOKEN;
import static com.ccsltd.twitter.service.Constant.RESOURCE_NOT_FOUND;
import static com.ccsltd.twitter.utils.Utils.sleepForMilliSeconds;

@RequiredArgsConstructor
@Slf4j
@Service
public class UnfollowService {

    private final Twitter twitter;
    private final FriendRepository friendRepository;
    private final ToUnfollowRepository toUnfollowRepository;
    private final UnfollowedRepository unfollowedRepository;
    private final ToFollowRepository toFollowRepository;
    private final EntityManager manager;

    private final int SLEEP_SECONDS = 60;

    public int identifyUnfollows() {
        StoredProcedureQuery unfollowFunction = manager.createNamedStoredProcedureQuery("createUsersToUnfollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        unfollowFunction.execute();

        return (int) unfollowFunction.getOutputParameterValue("followCount");
    }

    @Transactional
    public int unfollow(int unFollowLimit) {
        List<ToUnfollow> allToUnfollow = toUnfollowRepository.findAll();
        int maxToUnfollow = allToUnfollow.size();
        int actualToUnfollow = (maxToUnfollow >= unFollowLimit ? unFollowLimit : maxToUnfollow);
        AtomicInteger unfollowedCount = new AtomicInteger(0);
        List<ToUnfollow> toUnfollowList = allToUnfollow.subList(0, actualToUnfollow);

        log.info("Unfollowing '{}' users", toUnfollowList.size());

        Consumer<ToUnfollow> unfollowFriend = user -> {
            String screenName = user.getScreenName();

            try {
                twitter.destroyFriendship(screenName);
                unfollowedRepository.save(new UnFollowed(user.getId(), screenName));
                friendRepository.deleteByScreenName(screenName);
                toUnfollowRepository.deleteByScreenName(screenName);
                log.info("No '{}' - unfollowed '{}' ", unfollowedCount.incrementAndGet(), screenName);
                sleepForMilliSeconds(150);
                return;
            } catch (TwitterException te) {

                switch (te.getErrorCode()) {

                    case INVALID_TOKEN:
                        log.info("Invalid Token");
                        return;

                    case RESOURCE_NOT_FOUND:
                        friendRepository.deleteByScreenName(screenName);
                        toUnfollowRepository.deleteByScreenName(screenName);
                        unfollowedRepository.deleteByScreenName(screenName);
                        log.info("User doesn't exist '{}'", screenName);
                        return;

                    default:
                        log.info("Unhandled error code '{}'", te.getErrorCode());
                        return;
                }
            }
        };

        toUnfollowList.forEach(unfollowFriend);

        return toUnfollowRepository.findAll().size();
    }
}


package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.Unfollow;
import com.ccsltd.twitter.entity.Unfollowed;
import com.ccsltd.twitter.repository.FriendRepository;
import com.ccsltd.twitter.repository.UnfollowRepository;
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
import java.util.function.Consumer;

import static com.ccsltd.twitter.service.Constant.INVALID_TOKEN;
import static com.ccsltd.twitter.service.Constant.RESOURCE_NOT_FOUND;

@RequiredArgsConstructor
@Slf4j
@Service
public class UnfollowService {

    private final Twitter twitter;
    private final FriendRepository friendRepository;
    private final UnfollowRepository unfollowRepository;
    private final UnfollowedRepository unfollowedRepository;
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
        List<Unfollow> allToUnfollow = unfollowRepository.findAll();
        int maxToUnfollow = allToUnfollow.size();
        int actualToUnfollow = (maxToUnfollow >= unFollowLimit ? unFollowLimit : maxToUnfollow);

        List<Unfollow> unFollowList = allToUnfollow.subList(0, actualToUnfollow);

        Consumer<Unfollow> unfollowFriend = user -> {
            String screenName = user.getScreenName();

            try {
                twitter.destroyFriendship(screenName);
                unfollowedRepository.save(new Unfollowed(user.getTwitterId(), screenName));
                friendRepository.deleteByScreenName(screenName);
                unfollowRepository.deleteByScreenName(screenName);
                log.info("unfollowed '{}' ", screenName);
                return;
            } catch (TwitterException te) {

                switch (te.getErrorCode()) {

                    case INVALID_TOKEN:
                        log.info("Invalid Token");
                        return;

                    case RESOURCE_NOT_FOUND:
                        unfollowRepository.deleteByScreenName(screenName);
                        friendRepository.deleteByScreenName(screenName);
                        return;

                    default:
                        log.info("Unhandled error code '{}'", te.getErrorCode());
                        return;
                }
            }
        };

        unFollowList.forEach(unfollowFriend);

        return unfollowRepository.findAll().size();
    }
}


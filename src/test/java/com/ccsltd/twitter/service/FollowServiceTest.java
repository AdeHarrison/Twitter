package com.ccsltd.twitter.service;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ccsltd.twitter.entity.Follow;
import com.ccsltd.twitter.entity.FollowIgnore;
import com.ccsltd.twitter.entity.FollowPending;
import com.ccsltd.twitter.repository.FollowIgnoreRepository;
import com.ccsltd.twitter.repository.FollowPendingRepository;
import com.ccsltd.twitter.repository.FollowRepository;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.utils.Utils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private Twitter twitter;

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FollowPendingRepository followPendingRepository;

    @Mock
    private FollowIgnoreRepository followIgnoreRepository;

    @Mock
    private EntityManager manager;

    @Mock
    private Utils utils;

    @InjectMocks
    private FollowService underTest;

    @Mock
    private User user;

    @Mock
    private TwitterException twitterException;

    private Long twitterId = 12345L;
    private String screenName = "s1";
    private Follow follow = new Follow(twitterId, "n1", screenName, "l1", "d1", false, false, 0, 0,
            LocalDateTime.now());
    private List<Follow> follow1List = asList(follow);
    private List<Follow> emptyFollowList = asList();

    @Test
    public void follow_validFollow_folowRequest() throws TwitterException {
        String expected = "'0' Users remain to follow";

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        doReturn(user).when(twitter).createFriendship(screenName);
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userNotFound_TwitterExceptionThrown() throws TwitterException {
        String expected = "'0' Users remain to follow";

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(108);
        doNothing().when(followerRepository).deleteByScreenName(screenName);
        doNothing().when(followRepository).deleteByScreenName(screenName);
        doNothing().when(followIgnoreRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followerRepository, followIgnoreRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followerRepository).deleteByScreenName(screenName);
        inOrder.verify(followIgnoreRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followerRepository, followIgnoreRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedNotTracked_TwitterExceptionThrown() throws TwitterException {
        String expected = "'0' Users remain to follow";
        Optional<FollowPending> followPending = Optional.ofNullable(null);

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(twitterId)).thenReturn(followPending);
        when(followPendingRepository.save(any(FollowPending.class))).thenReturn(new FollowPending());
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followPendingRepository).findByTwitterId(twitterId);
        inOrder.verify(followPendingRepository).save(any(FollowPending.class));
        inOrder.verify(followRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followPendingRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndValid_TwitterExceptionThrown() throws TwitterException {
        String expected = "'0' Users remain to follow";
        FollowPending valid = new FollowPending();
        valid.setTimeStamp(LocalDateTime.now().minusDays(1));

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(twitterId)).thenReturn(of(valid));
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followPendingRepository).findByTwitterId(twitterId);
        inOrder.verify(followRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followPendingRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndExpired_TwitterExceptionThrown() throws TwitterException {
        String expected = "'0' Users remain to follow";
        FollowPending expired = new FollowPending();
        expired.setTimeStamp(LocalDateTime.now().minusDays(6));

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(twitterId)).thenReturn(of(expired));
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository, followIgnoreRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followPendingRepository).findByTwitterId(twitterId);
        inOrder.verify(followIgnoreRepository).save(any(FollowIgnore.class));
        inOrder.verify(followRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followPendingRepository, followIgnoreRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_rateLimitReached_TwitterExceptionThrown() throws TwitterException {
        String expected = "'1' Users remain to follow";

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(follow1List);
        when(twitter.createFriendship(screenName)).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(161);
        doNothing().when(utils).handleRateLimitBreach(anyInt(), anyInt());

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, utils);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(utils).handleRateLimitBreach(anyInt(), anyInt());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, utils);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_unhandledException_TwitterExceptionThrown() throws TwitterException {
        String expected = "'1' Users remain to follow";

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(follow1List);
        when(twitter.createFriendship(screenName)).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(9999);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, utils);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }
}

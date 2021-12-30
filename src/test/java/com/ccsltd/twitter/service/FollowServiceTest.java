package com.ccsltd.twitter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Test
    public void follow_valid_followRequest() throws TwitterException {
        int expected = 0;

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_equalsToFollow_0LeftToFollow() throws TwitterException {
        int expected = 0;

        when(followRepository.findAll()).thenReturn(createFollowList(2)).thenReturn(createFollowList(0));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(2);

        InOrder inOrder = inOrder(followRepository, twitter);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_lessToFollow_0LeftToFollow() throws TwitterException {
        int expected = 0;

        when(followRepository.findAll()).thenReturn(createFollowList(2)).thenReturn(createFollowList(0));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(3);

        InOrder inOrder = inOrder(followRepository, twitter);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_moreToFollow_1LeftToFollow() throws TwitterException {
        int expected = 1;

        when(followRepository.findAll()).thenReturn(createFollowList(3)).thenReturn(createFollowList(1));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(2);

        InOrder inOrder = inOrder(followRepository, twitter);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userNotFound_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(108);
        doNothing().when(followerRepository).deleteByScreenName(anyString());
        doNothing().when(followRepository).deleteByScreenName(anyString());
        doNothing().when(followIgnoreRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter, followerRepository, followIgnoreRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followerRepository).deleteByScreenName(anyString());
        inOrder.verify(followIgnoreRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followerRepository, followIgnoreRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedNotTracked_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        Optional<FollowPending> followPending = Optional.empty();

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(anyLong())).thenReturn(followPending);
        when(followPendingRepository.save(any(FollowPending.class))).thenReturn(new FollowPending());
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followPendingRepository).findByTwitterId(anyLong());
        inOrder.verify(followPendingRepository).save(any(FollowPending.class));
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followPendingRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndValid_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        FollowPending valid = new FollowPending();
        valid.setTimeStamp(LocalDateTime.now().minusDays(1));

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(anyLong())).thenReturn(Optional.of(valid));
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followPendingRepository).findByTwitterId(anyLong());
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followPendingRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndExpired_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        FollowPending expired = new FollowPending();
        expired.setTimeStamp(LocalDateTime.now().minusDays(6));

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(anyLong())).thenReturn(Optional.of(expired));
        doNothing().when(followRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository, followIgnoreRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followPendingRepository).findByTwitterId(anyLong());
        inOrder.verify(followIgnoreRepository).save(any(FollowIgnore.class));
        inOrder.verify(followRepository).deleteByScreenName(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followPendingRepository, followIgnoreRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_rateLimitReached_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        List<Follow> followList = createFollowList(3);

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(161);
        doNothing().when(utils).handleRateLimitBreach(anyInt(), anyInt());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter, utils);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(utils).handleRateLimitBreach(anyInt(), anyInt());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, utils);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_unhandledException_TwitterExceptionThrown() throws TwitterException {
        int expected = 1;

        when(followRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(1));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(9999);

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(followRepository, twitter, utils);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter);

        assertEquals(actual, expected);
    }

    private List<Follow> createFollowList(int maxObjects) {
        List<Follow> followList = new ArrayList<>();
        Follow follow = new Follow(123L, "n1", "s1", "l1", "d1", false, false, 0, 0, LocalDateTime.now());

        for (int i = 0; i < maxObjects; i++) {
            followList.add(follow);
        }

        return followList;
    }
}

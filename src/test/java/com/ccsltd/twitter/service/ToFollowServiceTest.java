package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.Followed;
import com.ccsltd.twitter.entity.FollowedPendingFollowBack;
import com.ccsltd.twitter.entity.ToFollow;
import com.ccsltd.twitter.repository.FollowedPendingFollowBackRepository;
import com.ccsltd.twitter.repository.FollowedRepository;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.ToFollowRepository;
import com.ccsltd.twitter.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToFollowServiceTest {

    @Mock
    private Twitter twitter;

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private ToFollowRepository toFollowRepository;

    @Mock
    private FollowedPendingFollowBackRepository followedPendingFollowBackRepository;

    @Mock
    private FollowedRepository followedRepository;

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

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_equalsToFollow_0LeftToFollow() throws TwitterException {
        int expected = 0;

        when(toFollowRepository.findAll()).thenReturn(createFollowList(2)).thenReturn(createFollowList(0));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(2);

        InOrder inOrder = inOrder(toFollowRepository, twitter);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_lessToFollow_0LeftToFollow() throws TwitterException {
        int expected = 0;

        when(toFollowRepository.findAll()).thenReturn(createFollowList(2)).thenReturn(createFollowList(0));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(3);

        InOrder inOrder = inOrder(toFollowRepository, twitter);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_moreToFollow_1LeftToFollow() throws TwitterException {
        int expected = 1;

        when(toFollowRepository.findAll()).thenReturn(createFollowList(3)).thenReturn(createFollowList(1));
        doReturn(user).when(twitter).createFriendship(anyString());
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(2);

        InOrder inOrder = inOrder(toFollowRepository, twitter);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userNotFound_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(108);
        doNothing().when(followerRepository).deleteByScreenName(anyString());
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());
        doNothing().when(followedRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter, followerRepository, followedRepository);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followerRepository).deleteByScreenName(anyString());
        inOrder.verify(followedRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter, followerRepository, followedRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedNotTracked_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        Optional<FollowedPendingFollowBack> followPending = Optional.empty();

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followedPendingFollowBackRepository.findById(anyLong())).thenReturn(followPending);
        when(followedPendingFollowBackRepository.save(any(FollowedPendingFollowBack.class))).thenReturn(new FollowedPendingFollowBack());
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter, followedPendingFollowBackRepository);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followedPendingFollowBackRepository).findById(anyLong());
        inOrder.verify(followedPendingFollowBackRepository).save(any(FollowedPendingFollowBack.class));
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter, followedPendingFollowBackRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndValid_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        FollowedPendingFollowBack valid = new FollowedPendingFollowBack();
        valid.setTimeStamp(LocalDateTime.now().minusDays(1));

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followedPendingFollowBackRepository.findById(anyLong())).thenReturn(Optional.of(valid));
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter, followedPendingFollowBackRepository);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followedPendingFollowBackRepository).findById(anyLong());
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter, followedPendingFollowBackRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndExpired_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        FollowedPendingFollowBack expired = new FollowedPendingFollowBack();
        expired.setTimeStamp(LocalDateTime.now().minusDays(6));

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(160);
        when(followedPendingFollowBackRepository.findById(anyLong())).thenReturn(Optional.of(expired));
        doNothing().when(toFollowRepository).deleteByScreenName(anyString());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter, followedPendingFollowBackRepository, followedRepository);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(followedPendingFollowBackRepository).findById(anyLong());
        inOrder.verify(followedRepository).save(any(Followed.class));
        inOrder.verify(toFollowRepository).deleteByScreenName(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter, followedPendingFollowBackRepository, followedRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_rateLimitReached_TwitterExceptionThrown() throws TwitterException {
        int expected = 0;
        List<ToFollow> toFollowList = createFollowList(3);

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(0));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(161);
        doNothing().when(utils).handleRateLimitBreach(anyInt(), anyInt());

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter, utils);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(utils).handleRateLimitBreach(anyInt(), anyInt());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter, utils);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_unhandledException_TwitterExceptionThrown() throws TwitterException {
        int expected = 1;

        when(toFollowRepository.findAll()).thenReturn(createFollowList(1)).thenReturn(createFollowList(1));
        when(twitter.createFriendship(anyString())).thenThrow(twitterException);
        when(twitterException.getErrorCode()).thenReturn(9999);

        int actual = underTest.follow(1);

        InOrder inOrder = inOrder(toFollowRepository, twitter, utils);
        inOrder.verify(toFollowRepository).findAll();
        inOrder.verify(twitter).createFriendship(anyString());
        inOrder.verify(toFollowRepository).findAll();

        verifyNoMoreInteractions(toFollowRepository, twitter);

        assertEquals(actual, expected);
    }

    private List<ToFollow> createFollowList(int maxObjects) {
        List<ToFollow> toFollowList = new ArrayList<>();
//        ToFollow toFollow = new ToFollow(123L, "n1", "s1", "l1", "d1", false, false, 0, 0, LocalDateTime.now());
//
//        for (int i = 0; i < maxObjects; i++) {
//            toFollowList.add(toFollow);
//        }

        return toFollowList;
    }
}

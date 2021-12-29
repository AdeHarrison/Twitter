package com.ccsltd.twitter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
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

    @InjectMocks
    FollowService underTest;

    @Test
    public void follow_validFollow_folowRequest() throws TwitterException {
        String screenName = "s1";
        Follow follow = new Follow(12345L, "n1", screenName, "l1", "d1", false, false, 0, 0, LocalDateTime.now());
        List<Follow> follow1List = Arrays.asList(follow);
        List<Follow> emptyFollowList = new ArrayList<>();
        String expected = "'0' Users remain to follow";

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        doReturn(new DatabaseUser()).when(twitter).createFriendship(screenName);
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
    public void follow_userNotFound_exceptionThrown() throws TwitterException {
        String screenName = "s1";
        Follow follow = new Follow(12345L, screenName, "s1", "l1", "d1", false, false, 0, 0, LocalDateTime.now());
        List<Follow> follow1List = Arrays.asList(follow);
        List<Follow> emptyFollowList = new ArrayList<>();
        String expected = "'0' Users remain to follow";
        TwitterException exception = mock(TwitterException.class);

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn(108);
        doNothing().when(followerRepository).deleteByScreenName(screenName);
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followerRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followerRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followerRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedNotTracked_ExceptionThrown() throws TwitterException {
        Long twitterId = 12345L;
        String screenName = "s1";
        Follow follow = new Follow(twitterId, "n1", "s1", "l1", "d1", false, false, 0, 0, LocalDateTime.now());
        List<Follow> follow1List = Arrays.asList(follow);
        List<Follow> emptyFollowList = new ArrayList<>();
        String expected = "'0' Users remain to follow";
        TwitterException exception = mock(TwitterException.class);
        Optional<FollowPending> followPending = Optional.ofNullable(null);

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn(160);
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

        verifyNoMoreInteractions(followRepository, twitter, followerRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndValid_ExceptionThrown() throws TwitterException {
        Long twitterId = 12345L;
        String screenName = "s1";
        Follow follow = new Follow(twitterId, "n1", "s1", "l1", "d1", false, false, 0, 0, LocalDateTime.now());
        List<Follow> follow1List = Arrays.asList(follow);
        List<Follow> emptyFollowList = new ArrayList<>();
        String expected = "'0' Users remain to follow";
        TwitterException exception = mock(TwitterException.class);
        FollowPending yesterday = new FollowPending();
        yesterday.setTimeStamp(LocalDateTime.now().minusDays(1));

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(twitterId)).thenReturn(Optional.ofNullable(yesterday));
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followPendingRepository).findByTwitterId(twitterId);
        inOrder.verify(followRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followerRepository);

        assertEquals(actual, expected);
    }

    @Test
    public void follow_userAlreadyFollowedTrackedAndExpired_ExceptionThrown() throws TwitterException {
        Long twitterId = 12345L;
        String screenName = "s1";
        Follow follow = new Follow(twitterId, "n1", "s1", "l1", "d1", false, false, 0, 0, LocalDateTime.now());
        List<Follow> follow1List = Arrays.asList(follow);
        List<Follow> emptyFollowList = new ArrayList<>();
        String expected = "'0' Users remain to follow";
        TwitterException exception = mock(TwitterException.class);
        FollowPending yesterday = new FollowPending();
        yesterday.setTimeStamp(LocalDateTime.now().minusDays(6));

        when(followRepository.findAll()).thenReturn(follow1List).thenReturn(emptyFollowList);
        when(twitter.createFriendship(screenName)).thenThrow(exception);
        when(exception.getErrorCode()).thenReturn(160);
        when(followPendingRepository.findByTwitterId(twitterId)).thenReturn(Optional.ofNullable(yesterday));
        doNothing().when(followRepository).deleteByScreenName(screenName);

        String actual = underTest.follow();

        InOrder inOrder = inOrder(followRepository, twitter, followPendingRepository, followIgnoreRepository);
        inOrder.verify(followRepository).findAll();
        inOrder.verify(twitter).createFriendship(screenName);
        inOrder.verify(followPendingRepository).findByTwitterId(twitterId);
        inOrder.verify(followIgnoreRepository).save(any(FollowIgnore.class));
        inOrder.verify(followRepository).deleteByScreenName(screenName);
        inOrder.verify(followRepository).findAll();

        verifyNoMoreInteractions(followRepository, twitter, followerRepository, followIgnoreRepository);

        assertEquals(actual, expected);
    }
}

class DatabaseUser implements User {

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getScreenName() {
        return null;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean isContributorsEnabled() {
        return false;
    }

    @Override
    public String getProfileImageURL() {
        return null;
    }

    @Override
    public String getBiggerProfileImageURL() {
        return null;
    }

    @Override
    public String getMiniProfileImageURL() {
        return null;
    }

    @Override
    public String getOriginalProfileImageURL() {
        return null;
    }

    @Override
    public String get400x400ProfileImageURL() {
        return null;
    }

    @Override
    public String getProfileImageURLHttps() {
        return null;
    }

    @Override
    public String getBiggerProfileImageURLHttps() {
        return null;
    }

    @Override
    public String getMiniProfileImageURLHttps() {
        return null;
    }

    @Override
    public String getOriginalProfileImageURLHttps() {
        return null;
    }

    @Override
    public String get400x400ProfileImageURLHttps() {
        return null;
    }

    @Override
    public boolean isDefaultProfileImage() {
        return false;
    }

    @Override
    public String getURL() {
        return null;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public int getFollowersCount() {
        return 0;
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public String getProfileBackgroundColor() {
        return null;
    }

    @Override
    public String getProfileTextColor() {
        return null;
    }

    @Override
    public String getProfileLinkColor() {
        return null;
    }

    @Override
    public String getProfileSidebarFillColor() {
        return null;
    }

    @Override
    public String getProfileSidebarBorderColor() {
        return null;
    }

    @Override
    public boolean isProfileUseBackgroundImage() {
        return false;
    }

    @Override
    public boolean isDefaultProfile() {
        return false;
    }

    @Override
    public boolean isShowAllInlineMedia() {
        return false;
    }

    @Override
    public int getFriendsCount() {
        return 0;
    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public int getFavouritesCount() {
        return 0;
    }

    @Override
    public int getUtcOffset() {
        return 0;
    }

    @Override
    public String getTimeZone() {
        return null;
    }

    @Override
    public String getProfileBackgroundImageURL() {
        return null;
    }

    @Override
    public String getProfileBackgroundImageUrlHttps() {
        return null;
    }

    @Override
    public String getProfileBannerURL() {
        return null;
    }

    @Override
    public String getProfileBannerRetinaURL() {
        return null;
    }

    @Override
    public String getProfileBannerIPadURL() {
        return null;
    }

    @Override
    public String getProfileBannerIPadRetinaURL() {
        return null;
    }

    @Override
    public String getProfileBannerMobileURL() {
        return null;
    }

    @Override
    public String getProfileBannerMobileRetinaURL() {
        return null;
    }

    @Override
    public String getProfileBanner300x100URL() {
        return null;
    }

    @Override
    public String getProfileBanner600x200URL() {
        return null;
    }

    @Override
    public String getProfileBanner1500x500URL() {
        return null;
    }

    @Override
    public boolean isProfileBackgroundTiled() {
        return false;
    }

    @Override
    public String getLang() {
        return null;
    }

    @Override
    public int getStatusesCount() {
        return 0;
    }

    @Override
    public boolean isGeoEnabled() {
        return false;
    }

    @Override
    public boolean isVerified() {
        return false;
    }

    @Override
    public boolean isTranslator() {
        return false;
    }

    @Override
    public int getListedCount() {
        return 0;
    }

    @Override
    public boolean isFollowRequestSent() {
        return false;
    }

    @Override
    public URLEntity[] getDescriptionURLEntities() {
        return new URLEntity[0];
    }

    @Override
    public URLEntity getURLEntity() {
        return null;
    }

    @Override
    public String[] getWithheldInCountries() {
        return new String[0];
    }

    @Override
    public int compareTo(User user) {
        return 0;
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return 0;
    }
}
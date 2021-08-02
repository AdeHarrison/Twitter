package com.ccsltd.twitter.endpoint;

import com.ccsltd.twitter.service.FollowerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TwitterUserControllerTest {

    @Mock
    private FollowerService followerService;

    @InjectMocks
    private FollowerController underTest;

    @Test
    public void getFollowers__allFollowersRetrieved() {
//        List<Follower> expected = Arrays.asList(new Follower(), new Follower());
//
//        Mockito.when(followerService.getFollowers()).thenReturn(expected);
//
//        List<Follower> actual = underTest.getFollowers();
//
//        assertEquals(actual.size(), expected.size());
    }
}

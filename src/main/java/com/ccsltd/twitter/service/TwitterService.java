package com.ccsltd.twitter.service;

import static org.apache.commons.lang3.StringUtils.join;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.FriendRepository;

import lombok.RequiredArgsConstructor;
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

@RequiredArgsConstructor
@Service
public class TwitterService {

    private final FollowerRepository followerRepository;
    private final FriendRepository friendRepository;

    public String initialiseData() {
        List<Follower> followers = getFollowers();
        followerRepository.saveAll(followers);

        pauseSeconds(10);

        List<Friend> friends = getFriends();
        friendRepository.saveAll(friends);

        return String.format("'%s' Followers created, '%s' Followers created", followers.size(), friends.size());
    }

    public String refreshData() {
        int newFolowerIds = updateFollowers();

        int newFriendIds = updateFriends();

        return String.format("'%s' Followers added, '%s' Followers added", newFolowerIds, newFriendIds);
    }

    private List<Follower> getFollowers() {
        Twitter twitter = new TwitterFactory().getInstance();
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        long sleepMilliSeconds = 60000l;
        List<Follower> allUsers = new ArrayList<>();
        int fakeCount = 0;

        followerRepository.deleteAll();
        serialiseList(partialUsers, "followers.ser", false);

        do {
            try {
                partialUsers = twitter.getFollowersList(screenName, nextCursor, maxResults);

                for (User user : partialUsers) {
                    //@formatter:off
                    Follower follower = Follower.builder().twitterId(
                            user.getId()).name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount()).build();
                    //@formatter:on

                    allUsers.add(follower);
                }

                serialiseList(partialUsers, "followers.ser", true);

                System.out.println("Total Followers retrieved: " + allUsers.size());

                //todo debug
                //                fakeCount++;
                //                if (fakeCount == 2) {
                //                    break;
                //                }

            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private int updateFollowers() {
        Twitter twitter = new TwitterFactory().getInstance();
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        long sleepMilliSeconds = 60000l;
        List<Long> newUsers = new ArrayList<>();
        int fakeCount = 0;

        do {
            try {
                partialUsers = twitter.getFollowersIDs(screenName, nextCursor, maxResults);

                for (Long id : partialUsers.getIDs()) {
                    Optional<Follower> user = followerRepository.findByTwitterId(id);

                    if (user.isEmpty()) {
                        newUsers.add(id);
                    }
                }
            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        String csv = join(newUsers, ",");

        try {
            ResponseList<User> usersToAdd = twitter.lookupUsers(538349876);
            //            ResponseList<User> usersToAdd = twitter.lookupUsers(csv);

            for (User user : usersToAdd) {
                Follower follower = Follower.builder().twitterId(user.getId()).name(user.getName())
                        .description(user.getDescription()).location(user.getLocation())
                        .followersCount(user.getFollowersCount()).friendsCount(user.getFriendsCount()).build();

                followerRepository.save(follower);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return newUsers.size();
    }

    private int updateFriends() {
        Twitter twitter = new TwitterFactory().getInstance();
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        long sleepMilliSeconds = 60000l;
        List<Long> newUsers = new ArrayList<>();
        int fakeCount = 0;

        do {
            try {
                partialUsers = twitter.getFriendsIDs(screenName, nextCursor, maxResults);

                for (Long id : partialUsers.getIDs()) {
                    Optional<Friend> user = friendRepository.findByTwitterId(id);

                    if (user.isEmpty()) {
                        newUsers.add(id);
                    }
                }
            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return newUsers.size();
    }

    private List<Friend> getFriends() {
        Twitter twitter = new TwitterFactory().getInstance();
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        long sleepMilliSeconds = 60000l;
        List<Friend> allUsers = new ArrayList<>();
        int fakeCount = 0;

        friendRepository.deleteAll();
        serialiseList(partialUsers, "friends.ser", false);

        do {
            try {
                partialUsers = twitter.getFriendsList(screenName, nextCursor, maxResults);

                for (User user : partialUsers) {
                    //@formatter:off
                    Friend friend = Friend.builder().twitterId(
                            user.getId())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount()).build();
                    //@formatter:on

                    allUsers.add(friend);
                }

                serialiseList(partialUsers, "friends.ser", true);

                System.out.println("Total Friends retrieved: " + allUsers.size());

                //todo debug
                //                fakeCount++;
                //                if (fakeCount == 1) {
                //                    break;
                //                }

            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private void serialiseList(List<?> list, String fileName, boolean append) {
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(fileName, append);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void pauseSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
}


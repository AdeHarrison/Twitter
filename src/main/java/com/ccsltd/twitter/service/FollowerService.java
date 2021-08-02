package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twitter4j.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FollowerService {

    private final FollowerRepository followerRepository;
    private final FriendRepository friendRepository;

    public List<Follower> getFollowers() {
        List<Follower> allFollowers = getAllFollowers();

        for (Follower follower : allFollowers) {
            try {
                followerRepository.save(follower);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        followerRepository.saveAll(allFollowers);

        return allFollowers;
    }

    public List<Friend> getFollowing() {
        List<Friend> allFollowing = getAllFriends();

        for (Friend friend : allFollowing) {
            try {
                friendRepository.save(friend);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return allFollowing;
    }

    public static void main(String[] args) {
//        FollowerService followerService = new FollowerService();
//
//        followerService.getAllFollowers();
    }

    private List<Follower> getAllFollowers() {
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
                    Follower follower = Follower.builder()
                            .twitterId(user.getId())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();

                    allUsers.add(follower);
                }

                serialiseList(partialUsers, "followers.ser", true);

                System.out.println("Total Followers retrieved: " + allUsers.size());
//                System.out.println(twitter.getRateLimitStatus());

                //todo debug
//                fakeCount++;
//                if (fakeCount == 2) {
//                    break;
//                }

            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                try {
                    Thread.sleep(sleepMilliSeconds);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private List<Friend> getAllFriends() {
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
                    Friend friend = Friend.builder()
                            .twitterId(user.getId())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();

//                    Follower follower = new Follower(user.getId(), user.getName(), user.getDescription(), user.getLocation(), user.getFollowersCount(), user.getFriendsCount());
                    allUsers.add(friend);
                }

                serialiseList(partialUsers, "friends.ser", true);

                System.out.println("Total Friends retrieved: " + allUsers.size());
//                System.out.println(twitter.getRateLimitStatus());

                //todo debug
//                fakeCount++;
//                if (fakeCount == 1) {
//                    break;
//                }

            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                try {
                    Thread.sleep(sleepMilliSeconds);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
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

//    private void deserialiseFollowersList(String fileName) {
//        ObjectOutputStream oos = null;
//        FileOutputStream fout = null;
//        List<Follower> list;
//        try {
//            fout = new FileOutputStream(fileName, true);
//            oos = new ObjectOutputStream(fout);
//            oos.writeObject(list);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            if (oos != null) {
//                oos.close();
//            }
//        }
//    }

}

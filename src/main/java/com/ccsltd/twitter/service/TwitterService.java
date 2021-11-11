package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.entity.ProcessControl;
import com.ccsltd.twitter.entity.Unfollow;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.FriendRepository;
import com.ccsltd.twitter.repository.ProcessControlRepository;
import com.ccsltd.twitter.repository.UnfollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twitter4j.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
@Slf4j
@Service
public class TwitterService {

    public static final String FOLLOWERS_SER = "followers.ser";
    public static final String FRIENDS_SER = "friends.ser";

    //    public static void main(String[] args) {
    //        TwitterService twitterService = new TwitterService(null,null, null, null);
    //        List<Follower> followers = twitterService.deserialiseList(FOLLOWERS_SER);
    //        List<Friend> friends = twitterService.deserialiseList(FRIENDS_SER);
    //        System.out.println(followers.size());
    //    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java twitter4j.examples.friendship.DestroyFriendship [screen name]");
            System.exit(-1);
        }
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.destroyFriendship(args[0]);
            System.out.println("Successfully unfollowed [" + args[0] + "].");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to unfollow: " + te.getMessage());
            System.exit(-1);
        }
    }

    private final FollowerRepository followerRepository;
    private final ProcessControlRepository processControlRepository;
    private final FriendRepository friendRepository;
    private final UnfollowRepository unfollowRepository;

    public String initialiseData(String status) {

        if ("prepare".equals(status)) {
            if (processControlRepository.existsById(1L)) {
                processControlRepository.deleteById(1L);
            }
            processControlRepository.save(new ProcessControl(1L, "initialise", "prepared"));

            return "initialise status is 'prepared' to execute";
        } else if ("execute".equals(status)) {
            ProcessControl currentProcess = processControlRepository.findByProcess("initialise");

            if (currentProcess == null || !"prepared".equals(currentProcess.getStatus())) {
                return "initialise status is not 'prepared'";
            }

            List<Follower> followers = getFollowers();
            serialiseList(followers, FOLLOWERS_SER, false);

            for (Follower follower : followers) {
                try {
                    followerRepository.save(follower);
                } catch (Exception cve) {
                    System.out.println(format("Duplicate Twitter Id {%s}", follower.getTwitterId()));
                }
            }

            pauseSeconds(10);

            List<Friend> friends = getFriends();
            serialiseList(friends, FRIENDS_SER, false);
            friendRepository.saveAll(friends);

            processControlRepository.deleteById(1L);

            return format("'%s' Followers created, '%s' Friends created", followers.size(), friends.size());
        } else {
            return format("invalid initialise status '%s'", status);
        }
    }

    public String refreshData() {
        int newFolowerIds = updateFollowers();
        int newFriendIds = updateFriends();

        return format("'%s' Followers added, '%s' Followers added", newFolowerIds, newFriendIds);
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
        boolean isDebug = "true".equals(System.getenv("debug"));

        followerRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFollowersList(screenName, nextCursor, maxResults);

                for (User user : partialUsers) {
                    //@formatter:off
                    Follower follower = Follower.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    allUsers.add(follower);
                }

                System.out.println("Total Followers retrieved: " + allUsers.size());

                //todo debug
                if (isDebug) {
                    fakeCount++;
                    if (fakeCount == 2) {
                        break;
                    }
                }
            } catch (TwitterException te) {
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

                    if (!user.isPresent()) {
                        newUsers.add(id);
                    }
                }
            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        if (newUsers.size() > 0) {
            try {
                long[] array = new long[newUsers.size()];
                int i = 0;
                for (Long newUser : newUsers) {
                    array[i] = newUsers.get(i);
                    i++;
                }

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);

                for (User user : usersToAdd) {
                    //@formatter:off
                    Follower follower = Follower.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    followerRepository.save(follower);
                }

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

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
        boolean isDebug = "true".equals(System.getenv("debug"));

        friendRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFriendsList(screenName, nextCursor, maxResults);

                for (User user : partialUsers) {
                    //@formatter:off
                    Friend friend = Friend.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    allUsers.add(friend);
                }

                System.out.println("Total Friends retrieved: " + allUsers.size());

                //todo debug
                if (isDebug) {
                    fakeCount++;
                    if (fakeCount == 2) {
                        break;
                    }
                }
            } catch (TwitterException te) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    public List<Unfollow> unfollow() throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();

        List<Unfollow> allToUnfollow = unfollowRepository.findAll();

        for (Unfollow unfollow : allToUnfollow) {
            String screenName = unfollow.getScreenName();

            twitter.destroyFriendship(screenName);
            unfollowRepository.deleteByScreenName(screenName);

            System.out.println(String.format("unfollowed '%s'", screenName));
        }

        return allToUnfollow;
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

                    if (!user.isPresent()) {
                        newUsers.add(id);
                    }
                }
            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        if (newUsers.size() > 0) {
            try {
                long[] array = new long[newUsers.size()];
                int i = 0;
                for (Long newUser : newUsers) {
                    array[i] = newUsers.get(i);
                    i++;
                }

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);

                for (User user : usersToAdd) {
                    //@formatter:off
                    Friend friend = Friend.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    friendRepository.save(friend);
                }

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return newUsers.size();
    }

    public String deserialise() {
        followerRepository.deleteAll();
        List<Follower> allFollowers = deserialiseList(FOLLOWERS_SER);
        followerRepository.saveAll(allFollowers);

        friendRepository.deleteAll();
        List<Friend> allFriends = deserialiseList(FRIENDS_SER);
        friendRepository.saveAll(allFriends);

        return format("'%s' Followers deserialised, '%s' Friends deserialised", allFollowers.size(), allFriends.size());
    }

    private static void serialiseList(List<?> list, String fileName, boolean append) {
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(fileName, append);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(list);
            oos.flush();
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

    private <T> T deserialiseList(String fileName) {
        List<T> list = null;

        try {
            FileInputStream fileIn = new FileInputStream(
                    "/home/ade/Documents/Local-Projects/local-java/Twitter/" + fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            list = (List<T>) in.readObject();
            in.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return (T) list;
    }

    private void pauseSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
}


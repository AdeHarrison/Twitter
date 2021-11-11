package com.ccsltd.twitter.service;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

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
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

@RequiredArgsConstructor
@Slf4j
@Service
public class TwitterService {

    public static final String FOLLOWERS_SER = "followers.ser";
    public static final String FRIENDS_SER = "friends.ser";

    //    public static void main(String[] args) {
    //        TwitterService twitterService = new TwitterService(null,null, null, null);
    //        List<Follower> followers = twitterService.deserializeList(FOLLOWERS_SER);
    //        List<Friend> friends = twitterService.deserializeList(FRIENDS_SER);
    //        System.out.println(followers.size());
    //    }

    private final FollowerRepository followerRepository;
    private final ProcessControlRepository processControlRepository;
    private final FriendRepository friendRepository;
    private final UnfollowRepository unfollowRepository;
    private final EntityManager manager;

    public String initialise(String status) {

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
            serializeList(followers, FOLLOWERS_SER, false);

            for (Follower follower : followers) {
                try {
                    followerRepository.save(follower);
                } catch (Exception cve) {
                    System.out.println(format("Duplicate Twitter Id '%s'", follower.getTwitterId()));
                }
            }

            pauseSeconds(10);

            List<Friend> friends = getFriends();
            serializeList(friends, FRIENDS_SER, false);
            friendRepository.saveAll(friends);

            processControlRepository.deleteById(1L);

            return format("'%s' Followers created, '%s' Friends created", followers.size(), friends.size());
        } else {
            return format("invalid initialise status '%s'", status);
        }
    }

    private List<Follower> getFollowers() {
        Twitter twitter = new TwitterFactory().getInstance();
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        long sleepMilliSeconds = 60000L;
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
                System.out.println(
                        "Rate limit reached getting Followers, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private List<Friend> getFriends() {
        Twitter twitter = new TwitterFactory().getInstance();
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        long sleepMilliSeconds = 60000L;
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
                System.out.println(
                        "Rate limit reached getting Friends, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    public String refresh() {
        int newFollowerIds = refreshFollowers();
        int newFriendIds = refreshFriends();
        StoredProcedureQuery storedProcedure = manager.createNamedStoredProcedureQuery("createUnfollower")
                .registerStoredProcedureParameter("unfollowerCount", Integer.class, ParameterMode.OUT);

        storedProcedure.execute();
        Integer unfollowerCount = (Integer) storedProcedure.getOutputParameterValue("unfollowerCount");
        List<Unfollow> unfollowList = unfollowRepository.findAll();

        StringBuilder unfollowerList = new StringBuilder();
        for (Unfollow unfollow : unfollowList) {
            unfollowerList.append(
                    String.format("%d,%s,%s\n", unfollow.getTwitterId(), unfollow.getName(), unfollow.getName()));
        }

        return format("'%s' new Followers added, '%s' new Friends added, '%s' new unfollowers\n\n%s", newFollowerIds,
                newFriendIds, unfollowerCount, unfollowerList.toString());
    }

    private int refreshFollowers() {
        Twitter twitter = new TwitterFactory().getInstance();
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        long sleepMilliSeconds = 60000L;
        List<Long> newUsers = new ArrayList<>();

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
                System.out.println(
                        "Rate limit reached refreshing Followers, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        if (newUsers.size() > 0) {
            try {
                long[] array = new long[newUsers.size()];
                AtomicInteger i = new AtomicInteger(0);

                newUsers.forEach(v -> array[i.getAndIncrement()] = v);

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);

                usersToAdd.forEach(v -> followerRepository.save(
                        Follower.builder().twitterId(v.getId()).screenName(v.getScreenName()).name(v.getName())
                                .description(v.getDescription()).location(v.getLocation())
                                .followersCount(v.getFollowersCount()).friendsCount(v.getFriendsCount())
                                .protectedTweets(v.isProtected()).build()));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return newUsers.size();
    }

    private int refreshFriends() {
        Twitter twitter = new TwitterFactory().getInstance();
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        long sleepMilliSeconds = 60000L;
        List<Long> newUsers = new ArrayList<>();

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
                System.out.println(
                        "Rate limit reached refreshing Friends, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        if (newUsers.size() > 0) {
            try {
                long[] array = new long[newUsers.size()];
                AtomicInteger i = new AtomicInteger(0);

                newUsers.forEach(v -> array[i.getAndIncrement()] = v);

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);

                usersToAdd.forEach(v -> friendRepository.save(
                        Friend.builder().twitterId(v.getId()).screenName(v.getScreenName()).name(v.getName())
                                .description(v.getDescription()).location(v.getLocation())
                                .followersCount(v.getFollowersCount()).friendsCount(v.getFriendsCount())
                                .protectedTweets((v.isProtected())).build()));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return newUsers.size();
    }

    @Transactional
    public List<Unfollow> unfollow() throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();

        List<Unfollow> allToUnfollow = unfollowRepository.findAll();

        int i = 1;

        for (Unfollow unfollow : allToUnfollow) {
            String screenName = unfollow.getScreenName();

            twitter.destroyFriendship(screenName);
            unfollowRepository.deleteByScreenName(screenName);
            friendRepository.deleteByScreenName(screenName);

            System.out.println(format("%d - unfollowed '%s'", i++, screenName));
        }

        return allToUnfollow;
    }

    public String reset() {
        followerRepository.deleteAll();
        List<Follower> allFollowers = deserializeList(FOLLOWERS_SER);
        followerRepository.saveAll(allFollowers);

        friendRepository.deleteAll();
        List<Friend> allFriends = deserializeList(FRIENDS_SER);
        friendRepository.saveAll(allFriends);

        return format("'%s' Followers deserialized, '%s' Friends deserialized", allFollowers.size(), allFriends.size());
    }

    private void serializeList(List<?> list, String fileName, boolean append) {
        ObjectOutputStream oos = null;

        try {
            FileOutputStream fout = new FileOutputStream(fileName, append);
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

    private <T> T deserializeList(String fileName) {
        List<T> list = null;

        try {
            String currentDir = System.getProperty("user.dir");
            FileInputStream fileIn = new FileInputStream(currentDir + "/" + fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            list = (List<T>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return (T) list;
    }

    private void pauseSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
}


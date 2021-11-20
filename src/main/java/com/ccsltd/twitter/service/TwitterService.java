package com.ccsltd.twitter.service;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ccsltd.twitter.entity.Fixed;
import com.ccsltd.twitter.entity.Follow;
import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.entity.ProcessControl;
import com.ccsltd.twitter.entity.Unfollow;
import com.ccsltd.twitter.repository.FixedRepository;
import com.ccsltd.twitter.repository.FollowRepository;
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
import twitter4j.User;

@RequiredArgsConstructor
@Slf4j
@Service
public class TwitterService {

    public static final String FIXED_SER = "fixed_%s.ser";
    public static final String FOLLOWERS_SER = "followers_%s.ser";
    public static final String FRIENDS_SER = "friends_%s.ser";

    //    public static void main(String[] args) {
    //        TwitterService twitterService = new TwitterService(null,null, null, null);
    //        List<Follower> followers = twitterService.deserializeList(FOLLOWERS_SER);
    //        List<Friend> friends = twitterService.deserializeList(FRIENDS_SER);
    //        System.out.println(followers.size());
    //    }

    private final Twitter twitter;
    private final FixedRepository fixedRepository;
    private final FollowerRepository followerRepository;
    private final ProcessControlRepository processControlRepository;
    private final FriendRepository friendRepository;
    private final UnfollowRepository unfollowRepository;
    private final FollowRepository followRepository;
    private final EntityManager manager;

    private final int SLEEP_SECONDS = 10;

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
            serializeList(followers, createFileName(FOLLOWERS_SER, "base"), false);

            for (Follower follower : followers) {
                try {
                    followerRepository.save(follower);
                } catch (Exception cve) {
                    System.out.println(format("Duplicate Twitter Id '%s'", follower.getTwitterId()));
                }
            }

            sleepForSeconds(SLEEP_SECONDS);

            List<Friend> friends = getFriends();
            serializeList(friends, createFileName(FRIENDS_SER, "base"), false);
            friendRepository.saveAll(friends);

            processControlRepository.deleteById(1L);

            return format("'%s' Followers created, '%s' Friends created", followers.size(), friends.size());
        } else {
            return format("invalid initialise status '%s'", status);
        }
    }

    private List<Follower> getFollowers() {
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        List<Follower> allUsers = new ArrayList<>();
        int fakeCount = 0;
        boolean isDebug = "true".equals(System.getenv("debug"));
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

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
                handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                sleptForSecondsTotal += SLEEP_SECONDS;
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private List<Friend> getFriends() {
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        List<Friend> allUsers = new ArrayList<>();
        int fakeCount = 0;
        boolean isDebug = "true".equals(System.getenv("debug"));
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

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
                handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                sleptForSecondsTotal += SLEEP_SECONDS;
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    public String refresh() {
        int newFollowers = refreshFollowers();
        int newFriends = refreshFriends();
        StoredProcedureQuery unfollowFunction = manager.createNamedStoredProcedureQuery("createUnfollow")
                .registerStoredProcedureParameter("unfollowCount", Integer.class, ParameterMode.OUT);

        unfollowFunction.execute();
        Integer unfollowCount = (Integer) unfollowFunction.getOutputParameterValue("unfollowCount");
        List<Unfollow> unfollowList = unfollowRepository.findAll();

        StringBuilder unfollowMessage = new StringBuilder();
        for (Unfollow unfollow : unfollowList) {
            unfollowMessage.append(
                    format("%d,%s,%s,%s\n", unfollow.getTwitterId(), unfollow.getName(), unfollow.getName(),
                            unfollow.getDescription()));
        }

        StoredProcedureQuery followFunction = manager.createNamedStoredProcedureQuery("createFollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        followFunction.execute();
        Integer followCount = (Integer) followFunction.getOutputParameterValue("followCount");
        List<Follow> followList = followRepository.findAll();

        StringBuilder followMessage = new StringBuilder();
        for (Follow follow : followList) {
            followMessage.append(format("%d,%s,%s,%s\n", follow.getTwitterId(), follow.getName(), follow.getName(),
                    follow.getDescription()));
        }

        return format(
                "'%s' new Followers added, '%s' new Friends added, '%s' new to unfollow, '%s' new to follow\n\n%s\n\n\n\n\n\n%s",
                newFollowers, newFriends, unfollowCount, followCount, unfollowMessage, followMessage);
    }

    private int refreshFollowers() {
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        List<Long> newUsers = new ArrayList<>();
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

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
                handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                sleptForSecondsTotal += SLEEP_SECONDS;
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
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        List<Long> newUsers = new ArrayList<>();
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

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
                handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                sleptForSecondsTotal += SLEEP_SECONDS;
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
    public List<Unfollow> unfollow() {
        List<Unfollow> allToUnfollow = unfollowRepository.findAll();

        Consumer<Unfollow> unfollowFriend = v -> {
            String screenName = v.getScreenName();
            boolean done = false;
            int rateLimitCount = 1;
            int sleptForSecondsTotal = 0;

            while (!done) {
                try {
                    twitter.destroyFriendship(screenName);
                    unfollowRepository.deleteByScreenName(screenName);
                    friendRepository.deleteByScreenName(screenName);
                    done = true;

                    System.out.println(format("unfollowed '%s'", screenName));
                } catch (TwitterException te) {
                    if (te.getErrorCode() == 34) {
                        break;
                    } else {
                        handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                        sleptForSecondsTotal += SLEEP_SECONDS;
                    }
                }
            }
        };

        allToUnfollow.forEach(unfollowFriend);

        return allToUnfollow;
    }

    @Transactional
    public List<Follow> follow() {
        List<Follow> allToFollow = followRepository.findAll();

        Consumer<Follow> createFriendship = v -> {
            String screenName = v.getScreenName();
            boolean done = false;
            int rateLimitCount = 1;
            int sleptForSecondsTotal = 0;

            while (!done) {
                try {
                    twitter.createFriendship(screenName);
                    followRepository.deleteByScreenName(v.getScreenName());
                    done = true;

                    log.info(format("followed '%s'", screenName));
                } catch (TwitterException te) {
                    if (te.getErrorCode() == 160) {
                        log.info(format("Already requested to follow '%s'", screenName));
                        break;
                    } else {
                        handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                        sleptForSecondsTotal += SLEEP_SECONDS;
                    }
                }
            }
        };

        allToFollow.forEach(createFriendship);

        return allToFollow;
    }

    public String reset(String resetTo) {
        String fixedFilename = createFileName(FIXED_SER, resetTo);
        List<Fixed> allFixed = deserializeList(fixedFilename);
        fixedRepository.deleteAll();
        fixedRepository.saveAll(allFixed);

        String followersFilename = createFileName(FOLLOWERS_SER, resetTo);
        List<Follower> allFollowers = deserializeList(followersFilename);
        followerRepository.deleteAll();
        followerRepository.saveAll(allFollowers);

        String friendsFilename = createFileName(FRIENDS_SER, resetTo);
        List<Friend> allFriends = deserializeList(friendsFilename);
        friendRepository.deleteAll();
        friendRepository.saveAll(allFriends);

        return format("'%s' Followers deserialized from '%s', '%s' Friends deserialized from '%s'", allFollowers.size(),
                followersFilename, allFriends.size(), friendsFilename);
    }

    public String snapshot(String snapshotTo) {
        String fixedFilename;
        String followersFilename;
        String friendsFilename;

        if ("now".equals(snapshotTo)) {
            fixedFilename = createNowFilename(FRIENDS_SER);
            followersFilename = createNowFilename(FOLLOWERS_SER);
            friendsFilename = createNowFilename(FRIENDS_SER);
        } else {
            fixedFilename = createFileName(FIXED_SER, snapshotTo);
            followersFilename = createFileName(FOLLOWERS_SER, snapshotTo);
            friendsFilename = createFileName(FRIENDS_SER, snapshotTo);
        }

        List<Fixed> fixedList = fixedRepository.findAll();
        serializeList(fixedList, fixedFilename, false);

        List<Follower> followerList = followerRepository.findAll();
        serializeList(followerList, followersFilename, false);

        List<Friend> friendList = friendRepository.findAll();
        serializeList(friendList, friendsFilename, false);

        return format("'%s' Followers serialized to '%s', '%s' Friends serialized to '%s'", followerList.size(),
                followersFilename, friendList.size(), friendsFilename);
    }

    private void handleRateLimitBreach(int rateLimitCount, int sleptForSecondsTotal) {
        log.info(format("Rate limit count = %s, waiting %d seconds. total slept time = %s", rateLimitCount,
                SLEEP_SECONDS, sleptForSecondsTotal));

        sleepForSeconds(SLEEP_SECONDS);
    }

    private String createFileName(String filenameFormat, String resetTo) {
        return format(filenameFormat, resetTo);
    }

    private String createNowFilename(String filenameFormat) {
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss").withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());

        return createFileName(filenameFormat, formatter.format(now));
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

    private void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}


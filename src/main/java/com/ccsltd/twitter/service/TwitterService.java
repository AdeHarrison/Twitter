package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.*;
import com.ccsltd.twitter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twitter4j.*;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.lang.System.getenv;

@RequiredArgsConstructor
@Slf4j
@Service
public class TwitterService {

    public static final String FIXED_SER = "fixed_%s.ser";
    public static final String FOLLOWER_SER = "followers_%s.ser";
    public static final String FRIEND_SER = "friends_%s.ser";
    private static final String FOLLOW_SER = "follow_%s.ser";
    private static final String PROCESS_CONTROL_SER = "process_control_%s.ser";

    private static final String UNFOLLOW_SER = "unfollow_%s.ser";

    private final Twitter twitter;
    private final FixedRepository fixedRepository;
    private final FollowerRepository followerRepository;
    private final ProcessControlRepository processControlRepository;
    private final FriendRepository friendRepository;
    private final UnfollowRepository unfollowRepository;
    private final FollowRepository followRepository;
    private final EntityManager manager;

    private final int SLEEP_SECONDS = 60;

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
            serializeList(followers, createFileName(FOLLOWER_SER, "base"), false);

            for (Follower follower : followers) {
                try {
                    followerRepository.save(follower);
                } catch (Exception cve) {
                    log.info("Duplicate Twitter Id '{}'", follower.getTwitterId());
                }
            }

            sleepForSeconds(SLEEP_SECONDS);

            List<Friend> friends = getFriends();
            serializeList(friends, createFileName(FRIEND_SER, "base"), false);
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
        boolean isDebug = "true".equals(getenv("twitter4j.debug"));
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

        followerRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFollowersList(screenName, nextCursor, maxResults);

                partialUsers.forEach(user -> allUsers.add(createFollower(user)));

                log.info("Total Followers retrieved: {}", allUsers.size());

                //todo debug
                if (isDebug) {
                    fakeCount++;
                    if (fakeCount == 1) {
                        break;
                    }
                }
            } catch (TwitterException te) {
                log.info("TwitterException error code = {}", te.getErrorCode());
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
        boolean isDebug = "true".equals(getenv("twitter4j.debug"));
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

        friendRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFriendsList(screenName, nextCursor, maxResults);

                partialUsers.forEach(user -> allUsers.add(createFriend(user)));

                log.info("Total Friends retrieved: {}", allUsers.size());

                //todo debug
                if (isDebug) {
                    fakeCount++;
                    if (fakeCount == 1) {
                        break;
                    }
                }
            } catch (TwitterException te) {
                log.info("TwitterException error code = {}", te.getErrorCode());
                handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                sleptForSecondsTotal += SLEEP_SECONDS;
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    public String refresh() {
        int newFollowers = refreshFollowers();
        int newFriends = refreshFriends();

        String logMessage = format("'%s' new Followers, '%s' new Friends", newFollowers, newFriends);

        log.info(logMessage);

        return logMessage;
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

            } catch (Exception e) {
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

    public String identifyUsersToFollow() {
        StoredProcedureQuery followFunction = manager.createNamedStoredProcedureQuery("createUsersToFollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        followFunction.execute();
        Integer followCount = (Integer) followFunction.getOutputParameterValue("followCount");

        String logMessage = format("'%s' Users to Follow", followCount);

        log.info(logMessage);

        return logMessage;
    }

    @Transactional
    public String follow() {
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
                    log.info("followed '{}'", screenName);
                } catch (TwitterException te) {

                    switch (te.getErrorCode()) {

                        case 108:
                            followRepository.deleteByScreenName(v.getScreenName());
                            done = true;
                            log.info("User doesn't exist '{}'", screenName);
                            return;

                        case 160:
                            followRepository.deleteByScreenName(v.getScreenName());
                            done = true;
                            log.info("Already requested to follow '{}'", screenName);
                            return;

                        case 161:
                            log.info("Failed to follow '{}', Follow limit reached - try later", screenName);
                            return;

                        default:
                            log.info("Unhandled error code '{}'", te.getErrorCode());
                            handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                            sleptForSecondsTotal += SLEEP_SECONDS;
                    }
                }
            }
        };

        allToFollow.forEach(createFriendship);

        String logMessage = format("'%s' Users remain to follow", followRepository.findAll().size());

        log.info(logMessage);

        return logMessage;
    }

    @Transactional
    public String unfollow() {
        if (!checkSafeToUnfollow()) {
            String logMessage = format("'%s' Users must be followed first", followRepository.findAll().size());

            log.info(logMessage);

            return logMessage;
        }

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

                    log.info("unfollowed '{}' ", screenName);
                } catch (TwitterException te) {
                    if (te.getErrorCode() == 34) {
                        unfollowRepository.deleteByScreenName(screenName);
                        friendRepository.deleteByScreenName(screenName);
                        done = true;
                        break;
                    } else {
                        handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                        sleptForSecondsTotal += SLEEP_SECONDS;
                    }
                }
            }
        };

        allToUnfollow.forEach(unfollowFriend);

        String logMessage = format("'%s' Users remain to unfollow", unfollowRepository.findAll().size());

        log.info(logMessage);

        return logMessage;
    }

    public String identifyUsersToUnfollow() {

        if (!checkSafeToUnfollow()) {
            String logMessage = format("'%s' Users must be followed first", followRepository.findAll().size());

            log.info(logMessage);

            return logMessage;
        }

        StoredProcedureQuery unfollowFunction = manager.createNamedStoredProcedureQuery("createUsersToUnfollow")
                .registerStoredProcedureParameter("followCount", Integer.class, ParameterMode.OUT);

        unfollowFunction.execute();
        Integer unfollowCount = (Integer) unfollowFunction.getOutputParameterValue("followCount");

        String logMessage = format("'%s' Users to Unfollow", unfollowCount);

        log.info(logMessage);

        return logMessage;
    }

    public String reset(String resetTo) {
        String fixedFilename = createFileName(FIXED_SER, resetTo);
        List<Fixed> fixedList = deserializeList(fixedFilename);
        fixedRepository.deleteAll();
        fixedRepository.saveAll(fixedList);

        String followFilename = createFileName(FOLLOW_SER, resetTo);
        List<Follow> followList = deserializeList(followFilename);
        followRepository.deleteAll();
        followRepository.saveAll(followList);

        String followerFilename = createFileName(FOLLOWER_SER, resetTo);
        List<Follower> followerList = deserializeList(followerFilename);
        followerRepository.deleteAll();
        followerRepository.saveAll(followerList);

        String friendFilename = createFileName(FRIEND_SER, resetTo);
        List<Friend> friendList = deserializeList(friendFilename);
        friendRepository.deleteAll();
        friendRepository.saveAll(friendList);

        String processControlFilename = createFileName(PROCESS_CONTROL_SER, resetTo);
        List<ProcessControl> processControlList = deserializeList(processControlFilename);
        processControlRepository.deleteAll();
        processControlRepository.saveAll(processControlList);

        String unfollowFilename = createFileName(UNFOLLOW_SER, resetTo);
        List<Unfollow> unfollowList = deserializeList(unfollowFilename);
        unfollowRepository.deleteAll();
        unfollowRepository.saveAll(unfollowList);

        //@formatter:off
        String logMessage = format(
                "'%s' Fixed deserialized to '%s', " +
                        "'%s' Follow deserialized to '%s', " +
                        "'%s' Followers deserialized to '%s', " +
                        "'%s' Friends deserialized to '%s', " +
                        "'%s' Process Control deserialized to '%s', " +
                        "'%s' Unfollow deserialized to '%s'",
                fixedList.size(), fixedFilename,
                followList.size(), followFilename,
                followerList.size(), followerFilename,
                friendList.size(), friendFilename,
                processControlList.size(), processControlFilename,
                unfollowList.size(), unfollowFilename);
        //@formatter:on

        log.info(logMessage);

        return logMessage;
    }

    public String snapshot(String snapshotTo) {
        String fixedFilename;
        String followFilename;
        String followerFilename;
        String friendFilename;
        String processControlFilename;
        String unfollowFilename;

        if ("now".equals(snapshotTo)) {
            fixedFilename = createNowFilename(FRIEND_SER);
            followFilename = createNowFilename(FOLLOW_SER);
            followerFilename = createNowFilename(FOLLOWER_SER);
            friendFilename = createNowFilename(FRIEND_SER);
            processControlFilename = createNowFilename(PROCESS_CONTROL_SER);
            unfollowFilename = createNowFilename(UNFOLLOW_SER);
        } else {
            fixedFilename = createFileName(FIXED_SER, snapshotTo);
            followFilename = createFileName(FOLLOW_SER, snapshotTo);
            followerFilename = createFileName(FOLLOWER_SER, snapshotTo);
            friendFilename = createFileName(FRIEND_SER, snapshotTo);
            processControlFilename = createFileName(PROCESS_CONTROL_SER, snapshotTo);
            unfollowFilename = createFileName(UNFOLLOW_SER, snapshotTo);
        }

        List<Fixed> fixedList = fixedRepository.findAll();
        serializeList(fixedList, fixedFilename, false);

        List<Follow> followList = followRepository.findAll();
        serializeList(followList, followFilename, false);

        List<Follower> followerList = followerRepository.findAll();
        serializeList(followerList, followerFilename, false);

        List<Friend> friendList = friendRepository.findAll();
        serializeList(friendList, friendFilename, false);

        List<ProcessControl> processControlList = processControlRepository.findAll();
        serializeList(processControlList, processControlFilename, false);

        List<Unfollow> unfollowList = unfollowRepository.findAll();
        serializeList(unfollowList, unfollowFilename, false);

        //@formatter:off
        String logMessage = format(
            "'%s' Fixed serialized to '%s', " +
            "'%s' Follow serialized to '%s', " +
            "'%s' Followers serialized to '%s', " +
            "'%s' Friends serialized to '%s', " +
            "'%s' Process Control serialized to '%s', " +
            "'%s' Unfollow serialized to '%s'",
            fixedList.size(), fixedFilename,
            followList.size(), followFilename,
            followerList.size(), followerFilename,
            friendList.size(), friendFilename,
            processControlList.size(), processControlFilename,
            unfollowList.size(), unfollowFilename);
        //@formatter:on

        log.info(logMessage);

        return logMessage;
    }

    private void handleRateLimitBreach(int rateLimitCount, int sleptForSecondsTotal) {
        log.info("Rate limit count = {}, waiting {} seconds. total slept time = {}", rateLimitCount, SLEEP_SECONDS,
                sleptForSecondsTotal);

        sleepForSeconds(SLEEP_SECONDS);
    }

    private LocalDateTime convertDateToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Follower createFollower(User user) {
        //@formatter:off
        return Follower.builder()
                .twitterId(user.getId())
                .name(user.getName())
                .screenName(user.getScreenName())
                .location(user.getLocation())
                .description(user.getDescription())
                .protectedTweets(user.isProtected())
                .verified(user.isVerified())
                .followersCount(user.getFollowersCount())
                .friendsCount(user.getFriendsCount())
                .created_at(convertDateToLocalDateTime(user.getCreatedAt()))
                .build();
        //@formatter:on
    }

    private Friend createFriend(User user) {
        //@formatter:off
        return Friend.builder()
                .twitterId(user.getId())
                .name(user.getName())
                .screenName(user.getScreenName())
                .location(user.getLocation())
                .description(user.getDescription())
                .protectedTweets(user.isProtected())
                .verified(user.isVerified())
                .followersCount(user.getFollowersCount())
                .friendsCount(user.getFriendsCount())
                .created_at(convertDateToLocalDateTime(user.getCreatedAt()))
                .build();
        //@formatter:on
    }

    private boolean checkSafeToUnfollow() {
        return followRepository.findAll().size() == 0;
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
        sleepForMilliSeconds(seconds * 1000);
    }

    private void sleepForMilliSeconds(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}


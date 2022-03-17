package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.*;
import com.ccsltd.twitter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twitter4j.*;

import javax.transaction.Transactional;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.lang.System.getenv;

@RequiredArgsConstructor
@Slf4j
@Service
public class TwitterService {

    public static final String BACKUP_DIR = "backup/";
    public static final String FIXED_SER = BACKUP_DIR + "fixed_%s.ser";
    private static final String TO_FOLLOW_SER = BACKUP_DIR + "to_follow_%s.ser";
    private static final String FOLLOWED_SER = BACKUP_DIR + "followed_%s.ser";
    public static final String FOLLOWED_PENDING_FOLLOW_BACK_SER = BACKUP_DIR + "followed_pending_follow_back_%s.ser";
    public static final String FOLLOWER_SER = BACKUP_DIR + "follower_%s.ser";
    public static final String FRIEND_SER = BACKUP_DIR + "friend_%s.ser";
    private static final String PROCESS_CONTROL_SER = BACKUP_DIR + "process_control_%s.ser";
    private static final String TO_UNFOLLOW_SER = BACKUP_DIR + "to_unfollow_%s.ser";
    private static final String UNFOLLOWED_SER = BACKUP_DIR + "unfollowed_%s.ser";
    public static final String LOG_SEPARATOR = "#######################################";
    public static final String SCREEN_NAME = "ade_bald";

    private final Twitter twitter;
    private final FixedRepository fixedRepository;
    private final ToFollowRepository toFollowRepository;
    private final FollowedRepository followedRepository;
    private final FollowedPendingFollowBackRepository followedPendingFollowBackRepository;
    private final FollowerRepository followerRepository;
    private final FriendRepository friendRepository;
    private final ProcessControlRepository processControlRepository;
    private final ToUnfollowRepository toUnfollowRepository;
    private final UnfollowedRepository unfollowedRepository;

    private final int SLEEP_SECONDS = 60;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
            .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public String initialise() {
        processControlRepository.save(new ProcessControl(1L, "initialise", "prepared"));

        return "initialise status is 'prepared' to execute";
    }

    @Transactional
    public String loadAllFollowers() {
        ProcessControl currentProcess = processControlRepository.findByProcess("initialise");

        if (currentProcess == null || !"prepared".equals(currentProcess.getStatus())) {
            return "initialise status is not 'prepared'";
        }

        PagableResponseList<User> partialUsers = null;
        long nextCursor = -1;
        List<Follower> followers = new ArrayList<>();
        int fakeCount = 1;
        boolean isDebug = "true".equals(getenv("twitter4j.debug"));
        int maxResults = (isDebug ? 10 : 200);
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

        followerRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFollowersList(SCREEN_NAME, nextCursor, maxResults);

                partialUsers.forEach(user -> followers.add(createFollower(user)));

                log.info("Total Followers retrieved: {}", followers.size());

                //todo debug
                if (isDebug) {
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

        serializeList(followers, createFilename(FOLLOWER_SER, "base"), false);
        followerRepository.saveAll(followers);

        processControlRepository.deleteById(1L);

        return format("'%s' Followers created", followers.size());
    }

    @Transactional
    public String loadAllFriends() {
        ProcessControl currentProcess = processControlRepository.findByProcess("initialise");

        if (currentProcess == null || !"prepared".equals(currentProcess.getStatus())) {
            return "initialise status is not 'prepared'";
        }

        PagableResponseList<User> partialUsers = null;
        long nextCursor = -1;
        List<Friend> friends = new ArrayList<>();
        int fakeCount = 1;
        boolean isDebug = "true".equals(getenv("twitter4j.debug"));
        int maxResults = (isDebug ? 10 : 200);
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;

        friendRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFriendsList(SCREEN_NAME, nextCursor, maxResults);

                partialUsers.forEach(user -> friends.add(createFriend(user)));

                log.info("Total Friend retrieved: {}", friends.size());

                //todo debug
                if (isDebug) {
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

        serializeList(friends, createFilename(FRIEND_SER, "base"), false);
        friendRepository.saveAll(friends);

        processControlRepository.deleteById(1L);

        return format("'%s' Friends created", friends.size());
    }

    public String createNewFollowersAndFriends() {
        String newFollowers = createNewFollowers();
        String newFriends = createNewFriends();

        String logMessage = newFollowers + ", " + newFriends;

        log.info(logMessage);

        return logMessage;
    }

    @Transactional
    public String createNewFollowers() {
        IDs partialUsers = null;
        long nextCursor = -1;
        int maxResults = 5000;
        List<Long> newUsers = new ArrayList<>();
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;
        int totalFollowersProcessed = 0;

        log.info(LOG_SEPARATOR);

        do {
            try {
                partialUsers = twitter.getFollowersIDs(SCREEN_NAME, nextCursor, maxResults);

                for (Long id : partialUsers.getIDs()) {
                    if (isNewFollower(id)) {
                        newUsers.add(id);
                        log.info("No '{}' - Identified new follower ID '{}'", ++totalFollowersProcessed, id);
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

        log.info(LOG_SEPARATOR);

        if (!newUsers.isEmpty()) {
            try {
                long[] array = new long[newUsers.size()];
                final AtomicInteger i = new AtomicInteger(0);

                newUsers.forEach(v -> array[i.getAndIncrement()] = v);

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);
                final AtomicInteger i2 = new AtomicInteger(0);

                usersToAdd.forEach(v -> {
                            followerRepository.save(
                                    Follower.builder().id(v.getId())
                                            .screenName(v.getScreenName())
                                            .name(v.getName())
                                            .description(v.getDescription())
                                            .location(v.getLocation())
                                            .followersCount(v.getFollowersCount())
                                            .friendsCount(v.getFriendsCount())
                                            .protectedTweets(v.isProtected())
                                            .build());
                            log.info("No '{}' - Saved new follower with screen name '{}'", i2.incrementAndGet(), v.getScreenName());
                        }
                );
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        String logMessage = format("'%s' new Followers", newUsers.size());

        log.info(logMessage);

        return logMessage;
    }

    @Transactional
    public String createNewFriends() {
        IDs partialUsers = null;
        long nextCursor = -1;
        int maxResults = 5000;
        List<Long> newUsers = new ArrayList<>();
        int rateLimitCount = 1;
        int sleptForSecondsTotal = 0;
        int totalFriendsProcessed = 0;

        log.info(LOG_SEPARATOR);

        do {
            try {
                partialUsers = twitter.getFriendsIDs(SCREEN_NAME, nextCursor, maxResults);

                for (Long id : partialUsers.getIDs()) {
                    if (isNewFriend(id)) {
                        newUsers.add(id);
                        log.info("No '{}' - Identified new friend ID '{}'", ++totalFriendsProcessed, id);
                    }
                }
            } catch (TwitterException e) {
                handleRateLimitBreach(rateLimitCount++, sleptForSecondsTotal);
                sleptForSecondsTotal += SLEEP_SECONDS;
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        log.info(LOG_SEPARATOR);

        if (!newUsers.isEmpty()) {
            try {
                //todo bug here when >100 new friends - use
                //newUsers = newUsers.subList(0, 99);

                long[] array = new long[newUsers.size()];
                AtomicInteger i = new AtomicInteger(0);

                newUsers.forEach(v -> array[i.getAndIncrement()] = v);

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);
                final AtomicInteger i2 = new AtomicInteger(0);

                usersToAdd.forEach(v -> {
                    friendRepository.save(
                                    Friend.builder().id(v.getId())
                                            .screenName(v.getScreenName())
                                            .name(v.getName())
                                            .description(v.getDescription())
                                            .location(v.getLocation())
                                            .followersCount(v.getFollowersCount())
                                            .friendsCount(v.getFriendsCount())
                                            .protectedTweets(v.isProtected())
                                            .build());
                            log.info("No '{}' - Saved new friend with screen name '{}'", i2.incrementAndGet(), v.getScreenName());
                        }
                );
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        String logMessage = format("'%s' new Friends", newUsers.size());

        log.info(logMessage);

        return logMessage;
    }

    public String reset(String resetTo) {
        String fixedFilename = createFilename(FIXED_SER, resetTo);
        String toFollowFilename = createFilename(TO_FOLLOW_SER, resetTo);
        String followedFilename = createFilename(FOLLOWED_SER, resetTo);
        String followPendingFilename = createFilename(FOLLOWED_PENDING_FOLLOW_BACK_SER, resetTo);
        String followerFilename = createFilename(FOLLOWER_SER, resetTo);
        String friendFilename = createFilename(FRIEND_SER, resetTo);
        String processControlFilename = createFilename(PROCESS_CONTROL_SER, resetTo);
        String toUnfollowFilename = createFilename(TO_UNFOLLOW_SER, resetTo);
        String unfollowedFilename = createFilename(UNFOLLOWED_SER, resetTo);

        List<Fixed> fixedList = deserializeList(fixedFilename);
        fixedRepository.deleteAll();
        fixedRepository.saveAll(fixedList);

        List<ToFollow> toFollowList = deserializeList(toFollowFilename);
        toFollowRepository.deleteAll();
        toFollowRepository.saveAll(toFollowList);

        List<Followed> followedList = deserializeList(followedFilename);
        followedRepository.deleteAll();
        followedRepository.saveAll(followedList);

        List<FollowedPendingFollowBack> followedPendingFollowBackList = deserializeList(followPendingFilename);
        followedPendingFollowBackRepository.deleteAll();
        followedPendingFollowBackRepository.saveAll(followedPendingFollowBackList);

        List<Follower> followerList = deserializeList(followerFilename);
        followerRepository.deleteAll();
        followerRepository.saveAll(followerList);

        List<Friend> friendList = deserializeList(friendFilename);
        friendRepository.deleteAll();
        friendRepository.saveAll(friendList);

        List<ProcessControl> processControlList = deserializeList(processControlFilename);
        processControlRepository.deleteAll();
        processControlRepository.saveAll(processControlList);

        List<ToUnfollow> unfollowList = deserializeList(toUnfollowFilename);
        toUnfollowRepository.deleteAll();
        toUnfollowRepository.saveAll(unfollowList);

        List<UnFollowed> unFollowedList = deserializeList(unfollowedFilename);
        unfollowedRepository.deleteAll();
        unfollowedRepository.saveAll(unFollowedList);

        //@formatter:off
        String logMessage = format(
                "'%s' Fixed deserialized from '%s', " +
                        "'%s' Follow deserialized from '%s', " +
                        "'%s' Followed deserialized from '%s', " +
                        "'%s' Follow Pending deserialized from '%s', " +
                        "'%s' Follower deserialized from '%s', " +
                        "'%s' Friend deserialized from '%s', " +
                        "'%s' Process Control deserialized from '%s', " +
                        "'%s' Unfollow deserialized from '%s'"+
                "'%s' Unfollowed deserialized from '%s'",
                fixedList.size(), fixedFilename,
                toFollowList.size(), toFollowFilename,
                followedList.size(), followedFilename,
                followedPendingFollowBackList.size(), followPendingFilename,
                followerList.size(), followerFilename,
                friendList.size(), friendFilename,
                processControlList.size(), processControlFilename,
                unfollowList.size(), toUnfollowFilename,
                unFollowedList.size(), unfollowedFilename);
        //@formatter:on

        log.info(logMessage);

        return logMessage;
    }

    public String snapshot(String snapshotTo) {
        String fixedFilename;
        String toFollowFilename;
        String followedFilename;
        String followPendingFilename;
        String followerFilename;
        String friendFilename;
        String processControlFilename;
        String toUnfollowFilename;
        String unfollowedFilename;

        if ("now".equals(snapshotTo)) {
            snapshotTo = formatter.format(Instant.now());
        }

        fixedFilename = createFilename(FIXED_SER, snapshotTo);
        toFollowFilename = createFilename(TO_FOLLOW_SER, snapshotTo);
        followedFilename = createFilename(FOLLOWED_SER, snapshotTo);
        followPendingFilename = createFilename(FOLLOWED_PENDING_FOLLOW_BACK_SER, snapshotTo);
        followerFilename = createFilename(FOLLOWER_SER, snapshotTo);
        friendFilename = createFilename(FRIEND_SER, snapshotTo);
        processControlFilename = createFilename(PROCESS_CONTROL_SER, snapshotTo);
        toUnfollowFilename = createFilename(TO_UNFOLLOW_SER, snapshotTo);
        unfollowedFilename = createFilename(UNFOLLOWED_SER, snapshotTo);

        List<Fixed> fixedList = fixedRepository.findAll();
        serializeList(fixedList, fixedFilename, false);

        List<ToFollow> toFollowList = toFollowRepository.findAll();
        serializeList(toFollowList, toFollowFilename, false);

        List<Followed> followedList = followedRepository.findAll();
        serializeList(followedList, followedFilename, false);

        List<FollowedPendingFollowBack> followedPendingFollowBackList = followedPendingFollowBackRepository.findAll();
        serializeList(followedPendingFollowBackList, followPendingFilename, false);

        List<Follower> followerList = followerRepository.findAll();
        serializeList(followerList, followerFilename, false);

        List<Friend> friendList = friendRepository.findAll();
        serializeList(friendList, friendFilename, false);

        List<ProcessControl> processControlList = processControlRepository.findAll();
        serializeList(processControlList, processControlFilename, false);

        List<ToUnfollow> unfollowList = toUnfollowRepository.findAll();
        serializeList(unfollowList, toUnfollowFilename, false);

        List<UnFollowed> unFollowedList = unfollowedRepository.findAll();
        serializeList(unFollowedList, unfollowedFilename, false);

        //@formatter:off
        String logMessage = format(
            "'%s' Fixed serialized to '%s', " +
            "'%s' To Follow serialized to '%s', " +
            "'%s' Followed serialized to '%s', " +
            "'%s' Follow Pending serialized to '%s', " +
            "'%s' Follower serialized to '%s', " +
            "'%s' Friend serialized to '%s', " +
            "'%s' Process Control serialized to '%s', " +
            "'%s' To Unfollow serialized to '%s'" +
            "'%s' Unfollowed serialized to '%s'",
            fixedList.size(), fixedFilename,
            toFollowList.size(), toFollowFilename,
            followedList.size(), followedFilename,
            followedPendingFollowBackList.size(), followPendingFilename,
            followerList.size(), followerFilename,
            friendList.size(), friendFilename,
            processControlList.size(), processControlFilename,
            unfollowList.size(), toUnfollowFilename,
            unFollowedList.size(), unfollowedFilename);
        //@formatter:on

        log.info(logMessage);

        return logMessage;
    }

    private boolean isNewFollower(Long id) {
        return followerRepository.findById(id).isEmpty();
    }

    private boolean isNewFriend(Long id) {
        return friendRepository.findById(id).isEmpty();
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
                .id(user.getId())
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
                .id(user.getId())
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

    private String createFilename(String filenameFormat, String resetTo) {
        return format(filenameFormat, resetTo);
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


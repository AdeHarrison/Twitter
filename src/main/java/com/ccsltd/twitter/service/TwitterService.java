package com.ccsltd.twitter.service;

import static java.lang.String.format;
import static java.lang.System.getenv;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.ccsltd.twitter.entity.Fixed;
import com.ccsltd.twitter.entity.Follow;
import com.ccsltd.twitter.entity.FollowIgnore;
import com.ccsltd.twitter.entity.FollowPending;
import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.entity.ProcessControl;
import com.ccsltd.twitter.entity.Unfollow;
import com.ccsltd.twitter.entity.Unfollowed;
import com.ccsltd.twitter.repository.FixedRepository;
import com.ccsltd.twitter.repository.FollowIgnoreRepository;
import com.ccsltd.twitter.repository.FollowPendingRepository;
import com.ccsltd.twitter.repository.FollowRepository;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.FriendRepository;
import com.ccsltd.twitter.repository.ProcessControlRepository;
import com.ccsltd.twitter.repository.UnfollowRepository;
import com.ccsltd.twitter.repository.UnfollowedRepository;

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
    private static final String FOLLOW_SER = "follow_%s.ser";
    private static final String FOLLOW_IGNORE_SER = "follow_ignore_%s.ser";
    public static final String FOLLOW_PENDING_SER = "follow_pending%s.ser";
    public static final String FOLLOWER_SER = "follower_%s.ser";
    public static final String FRIEND_SER = "friends_%s.ser";
    private static final String PROCESS_CONTROL_SER = "process_control_%s.ser";
    private static final String UNFOLLOW_SER = "unfollow_%s.ser";
    private static final String UNFOLLOWED_SER = "unfollowed_%s.ser";

    private final Twitter twitter;
    private final FixedRepository fixedRepository;
    private final FollowRepository followRepository;
    private final FollowIgnoreRepository followIgnoreRepository;
    private final FollowPendingRepository followPendingRepository;
    private final FollowerRepository followerRepository;
    private final FriendRepository friendRepository;
    private final ProcessControlRepository processControlRepository;
    private final UnfollowRepository unfollowRepository;
    private final UnfollowedRepository unfollowedRepository;

    private final int SLEEP_SECONDS = 60;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
            .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

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
            serializeList(followers, createFilename(FOLLOWER_SER, "base"), false);

            for (Follower follower : followers) {
                try {
                    followerRepository.save(follower);
                } catch (Exception cve) {
                    log.info("Duplicate Twitter Id '{}'", follower.getTwitterId());
                }
            }

            sleepForSeconds(SLEEP_SECONDS);

            List<Friend> friends = getFriends();
            serializeList(friends, createFilename(FRIEND_SER, "base"), false);
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
                        log.info("Identified new follower ID '{}'", id);
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
                        log.info("Identified new friend ID '{}'", id);
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

    public String reset(String resetTo) {
        String fixedFilename = createFilename(FIXED_SER, resetTo);
        String followFilename = createFilename(FOLLOW_SER, resetTo);
        String followIgnoreFilename = createFilename(FOLLOW_IGNORE_SER, resetTo);
        String followPendingFilename = createFilename(FOLLOW_PENDING_SER, resetTo);
        String followerFilename = createFilename(FOLLOWER_SER, resetTo);
        String friendFilename = createFilename(FRIEND_SER, resetTo);
        String processControlFilename = createFilename(PROCESS_CONTROL_SER, resetTo);
        String unfollowFilename = createFilename(UNFOLLOW_SER, resetTo);
        String unfollowedFilename = createFilename(UNFOLLOWED_SER, resetTo);

        List<Fixed> fixedList = deserializeList(fixedFilename);
        fixedRepository.deleteAll();
        fixedRepository.saveAll(fixedList);

        List<Follow> followList = deserializeList(followFilename);
        followRepository.deleteAll();
        followRepository.saveAll(followList);

        List<FollowIgnore> followIgnoreList = deserializeList(followIgnoreFilename);
        followIgnoreRepository.deleteAll();
        followIgnoreRepository.saveAll(followIgnoreList);

        List<FollowPending> followPendingList = deserializeList(followPendingFilename);
        followPendingRepository.deleteAll();
        followPendingRepository.saveAll(followPendingList);

        List<Follower> followerList = deserializeList(followerFilename);
        followerRepository.deleteAll();
        followerRepository.saveAll(followerList);

        List<Friend> friendList = deserializeList(friendFilename);
        friendRepository.deleteAll();
        friendRepository.saveAll(friendList);

        List<ProcessControl> processControlList = deserializeList(processControlFilename);
        processControlRepository.deleteAll();
        processControlRepository.saveAll(processControlList);

        List<Unfollow> unfollowList = deserializeList(unfollowFilename);
        unfollowRepository.deleteAll();
        unfollowRepository.saveAll(unfollowList);

        List<Unfollowed> unfollowedList = deserializeList(unfollowedFilename);
        unfollowedRepository.deleteAll();
        unfollowedRepository.saveAll(unfollowedList);

        //@formatter:off
        //@formatter:off
        String logMessage = format(
                "'%s' Fixed deserialized from '%s', " +
                        "'%s' Follow deserialized from '%s', " +
                        "'%s' Follow Ignore deserialized from '%s', " +
                        "'%s' Follow Pending deserialized from '%s', " +
                        "'%s' Follower deserialized from '%s', " +
                        "'%s' Friend deserialized from '%s', " +
                        "'%s' Process Control deserialized from '%s', " +
                        "'%s' Unfollow deserialized from '%s'"+
                "'%s' Unfollowed deserialized from '%s'",
                fixedList.size(), fixedFilename,
                followList.size(), followFilename,
                followIgnoreList.size(), followIgnoreFilename,
                followPendingList.size(), followPendingFilename,
                followerList.size(), followerFilename,
                friendList.size(), friendFilename,
                processControlList.size(), processControlFilename,
                unfollowList.size(), unfollowFilename,
                unfollowedList.size(), unfollowedFilename);
        //@formatter:on

        log.info(logMessage);

        return logMessage;
    }

    public String snapshot(String snapshotTo) {
        String fixedFilename;
        String followFilename;
        String followIgnoreFilename;
        String followPendingFilename;
        String followerFilename;
        String friendFilename;
        String processControlFilename;
        String unfollowFilename;
        String unfollowedFilename;

        if ("now".equals(snapshotTo)) {
            snapshotTo = formatter.format(Instant.now());
        }

        fixedFilename = createFilename(FIXED_SER, snapshotTo);
        followFilename = createFilename(FOLLOW_SER, snapshotTo);
        followIgnoreFilename = createFilename(FOLLOW_IGNORE_SER, snapshotTo);
        followPendingFilename = createFilename(FOLLOW_PENDING_SER, snapshotTo);
        followerFilename = createFilename(FOLLOWER_SER, snapshotTo);
        friendFilename = createFilename(FRIEND_SER, snapshotTo);
        processControlFilename = createFilename(PROCESS_CONTROL_SER, snapshotTo);
        unfollowFilename = createFilename(UNFOLLOW_SER, snapshotTo);
        unfollowedFilename = createFilename(UNFOLLOWED_SER, snapshotTo);

        List<Fixed> fixedList = fixedRepository.findAll();
        serializeList(fixedList, fixedFilename, false);

        List<Follow> followList = followRepository.findAll();
        serializeList(followList, followFilename, false);

        List<FollowIgnore> followIgnoreList = followIgnoreRepository.findAll();
        serializeList(followIgnoreList, followIgnoreFilename, false);

        List<FollowPending> followPendingList = followPendingRepository.findAll();
        serializeList(followPendingList, followPendingFilename, false);

        List<Follower> followerList = followerRepository.findAll();
        serializeList(followerList, followerFilename, false);

        List<Friend> friendList = friendRepository.findAll();
        serializeList(friendList, friendFilename, false);

        List<ProcessControl> processControlList = processControlRepository.findAll();
        serializeList(processControlList, processControlFilename, false);

        List<Unfollow> unfollowList = unfollowRepository.findAll();
        serializeList(unfollowList, unfollowFilename, false);

        List<Unfollowed> unfollowedList = unfollowedRepository.findAll();
        serializeList(unfollowedList, unfollowedFilename, false);

        //@formatter:off
        String logMessage = format(
            "'%s' Fixed serialized to '%s', " +
            "'%s' Follow serialized to '%s', " +
            "'%s' Follow Ignore serialized to '%s', " +
            "'%s' Follow Pending serialized to '%s', " +
            "'%s' Follower serialized to '%s', " +
            "'%s' Friend serialized to '%s', " +
            "'%s' Process Control serialized to '%s', " +
            "'%s' Unfollow serialized to '%s'" +
            "'%s' Unfollowed serialized to '%s'",
            fixedList.size(), fixedFilename,
            followList.size(), followFilename,
            followIgnoreList.size(), followIgnoreFilename,
            followPendingList.size(), followPendingFilename,
            followerList.size(), followerFilename,
            friendList.size(), friendFilename,
            processControlList.size(), processControlFilename,
            unfollowList.size(), unfollowFilename,
            unfollowedList.size(), unfollowedFilename);
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

    private String createFilename(String filenameFormat, String resetTo) {
        return format(filenameFormat, resetTo);
    }

    private String createNowFilename(String filenameFormat) {
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss").withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());

        return createFilename(filenameFormat, formatter.format(now));
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

